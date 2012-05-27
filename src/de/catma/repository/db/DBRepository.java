package de.catma.repository.db;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import de.catma.backgroundservice.BackgroundServiceProvider;
import de.catma.backgroundservice.DefaultProgressCallable;
import de.catma.backgroundservice.ExecutionListener;
import de.catma.core.document.Corpus;
import de.catma.core.document.source.ISourceDocument;
import de.catma.core.document.source.SourceDocument;
import de.catma.core.document.standoffmarkup.staticmarkup.StaticMarkupCollection;
import de.catma.core.document.standoffmarkup.staticmarkup.StaticMarkupCollectionReference;
import de.catma.core.document.standoffmarkup.usermarkup.IUserMarkupCollection;
import de.catma.core.document.standoffmarkup.usermarkup.UserMarkupCollectionReference;
import de.catma.core.tag.ITagLibrary;
import de.catma.core.tag.PropertyDefinition;
import de.catma.core.tag.PropertyPossibleValueList;
import de.catma.core.tag.TagDefinition;
import de.catma.core.tag.TagLibraryReference;
import de.catma.core.tag.TagManager;
import de.catma.core.tag.TagManager.TagManagerEvent;
import de.catma.core.tag.TagsetDefinition;
import de.catma.core.tag.Version;
import de.catma.core.user.User;
import de.catma.core.util.CloseSafe;
import de.catma.core.util.IDGenerator;
import de.catma.core.util.Pair;
import de.catma.indexer.IndexedRepository;
import de.catma.indexer.Indexer;
import de.catma.repository.db.model.DBPropertyDefPossibleValue;
import de.catma.repository.db.model.DBPropertyDefinition;
import de.catma.repository.db.model.DBSourceDocument;
import de.catma.repository.db.model.DBTagDefinition;
import de.catma.repository.db.model.DBTagLibrary;
import de.catma.repository.db.model.DBTagsetDefinition;
import de.catma.repository.db.model.DBUser;
import de.catma.repository.db.model.DBUserMarkupCollection;
import de.catma.repository.db.model.DBUserSourceDocument;
import de.catma.repository.db.model.DBUserTagLibrary;
import de.catma.repository.db.model.DBUserUserMarkupCollection;
import de.catma.serialization.SerializationHandlerFactory;
import de.catma.serialization.TagLibrarySerializationHandler;

public class DBRepository implements IndexedRepository {
	//TODO: handle sqlstate 40001 deadlock
	
	private String name;
	private DBSourceDocumentHandler dbSourceDocumentHandler;
	private Indexer indexer;
	private SerializationHandlerFactory serializationHandlerFactory;
	private boolean authenticationRequired;
	private BackgroundServiceProvider backgroundServiceProvider;
	private TagManager tagManager;

	private SessionFactory sessionFactory; 
	private Configuration hibernateConfig;
	
	private Set<Corpus> corpora;
	private Map<String,ISourceDocument> sourceDocumentsByID;
	private Set<TagLibraryReference> tagLibraryReferences;
	private Map<String,DBTagLibrary> tagLibrariesByID;

	private DBUser currentUser;
	private PropertyChangeSupport propertyChangeSupport;
	private ServiceRegistry serviceRegistry;
	private Map<String,Integer> uuidToDBid;
	private IDGenerator idGenerator;
	
	private boolean tagManagerListenersEnabled = true;
	private PropertyChangeListener tagsetDefinitionChangedListener;
	private PropertyChangeListener tagDefinitionChangedListener;

	public DBRepository(
			String name, 
			String repoFolderPath, 
			boolean authenticationRequired, 
			TagManager tagManager, 
			BackgroundServiceProvider backgroundServiceProvider,
			Indexer indexer, 
			SerializationHandlerFactory serializationHandlerFactory) {
		
		this.propertyChangeSupport = new PropertyChangeSupport(this);
		this.idGenerator = new IDGenerator();
		this.name = name;
		this.dbSourceDocumentHandler = 
				new DBSourceDocumentHandler(repoFolderPath);
		this.authenticationRequired = authenticationRequired;
		this.tagManager = tagManager;
		this.backgroundServiceProvider = backgroundServiceProvider;
		this.indexer = indexer;
		this.serializationHandlerFactory = serializationHandlerFactory;
		hibernateConfig = new Configuration();
		hibernateConfig.configure(
				this.getClass().getPackage().getName().replace('.', '/') 
				+ "/hibernate.cfg.xml");
		
		ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder();
		serviceRegistryBuilder.applySettings(hibernateConfig.getProperties());
		serviceRegistry = 
				serviceRegistryBuilder.buildServiceRegistry();
		
		corpora = new HashSet<Corpus>();
		sourceDocumentsByID = new HashMap<String, ISourceDocument>();
		tagLibraryReferences = new HashSet<TagLibraryReference>();
		tagLibrariesByID = new HashMap<String, DBTagLibrary>();
		uuidToDBid = new HashMap<String, Integer>();
		
		tagsetDefinitionChangedListener = new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				
				if (!tagManagerListenersEnabled) {
					return;
				}
				
				if (evt.getOldValue() == null) {
					@SuppressWarnings("unchecked")
					Pair<ITagLibrary, TagsetDefinition> args = 
							(Pair<ITagLibrary, TagsetDefinition>)evt.getNewValue();
					saveTagsetDefinition(args.getFirst(), args.getSecond());
				}
				else if (evt.getNewValue() == null) {
					@SuppressWarnings("unchecked")
					Pair<ITagLibrary, TagsetDefinition> args = 
							(Pair<ITagLibrary, TagsetDefinition>)evt.getOldValue();
					removeTagsetDefinition(args.getSecond());
				}
				else {
					updateTagsetDefinition((TagsetDefinition)evt.getNewValue());
				}
			}
		};
		
		tagManager.addPropertyChangeListener(
				TagManagerEvent.tagsetDefinitionChanged,
				tagsetDefinitionChangedListener);
		
		tagDefinitionChangedListener = new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				
				if (!tagManagerListenersEnabled) {
					return;
				}

				if (evt.getOldValue() == null) {
					@SuppressWarnings("unchecked")
					Pair<TagsetDefinition, TagDefinition> args = 
							(Pair<TagsetDefinition, TagDefinition>)evt.getNewValue();
					saveTagDefinition(args.getFirst(), args.getSecond());
				}
				else if (evt.getNewValue() == null) {
					@SuppressWarnings("unchecked")
					Pair<TagsetDefinition, TagDefinition> args = 
							(Pair<TagsetDefinition, TagDefinition>)evt.getOldValue();
					saveTagDefinition(args.getFirst(), args.getSecond());
					removeTagDefinition(args.getSecond());
				}
				else {
					updateTagDefinition((TagDefinition)evt.getNewValue());
				}
			}

		};
		
		tagManager.addPropertyChangeListener(
				TagManagerEvent.tagDefinitionChanged,
				tagDefinitionChangedListener);	
	}
	
	private void updateTagDefinition(final TagDefinition tagDefinition) {
		
		backgroundServiceProvider.submit(
			new DefaultProgressCallable<Void>() {
				public Void call() throws Exception {
					Session session = sessionFactory.openSession();
					try {
						session.beginTransaction();
						
						DBTagDefinition dbTagDefinition = 
							(DBTagDefinition)session.load(
								DBTagDefinition.class, 
								uuidToDBid.get(tagDefinition.getID()));
						dbTagDefinition.setName(
								tagDefinition.getName());
						PropertyDefinition colorDefinition = 
							tagDefinition.getPropertyDefinitionByName(
								PropertyDefinition.SystemPropertyName.catma_displaycolor.name());
						DBPropertyDefinition dbColorDefinition =
								(DBPropertyDefinition)session.load(
										DBPropertyDefinition.class,
										uuidToDBid.get(colorDefinition.getID()));
						dbColorDefinition.setSingleValue(
								tagDefinition.getColor());
						dbTagDefinition.setVersion(
							tagDefinition.getVersion().getDate());
						
						session.update(dbTagDefinition);
						
						session.getTransaction().commit();
						
						return null;
					}
					catch (Exception e) {
						try {
							if (session.getTransaction().isActive()) {
								session.getTransaction().rollback();
							}
						}
						catch(Exception notOfInterest){}
						throw new Exception(e);
					}
					finally {
						CloseSafe.close(new ClosableSession(session));
					}
				}
			}, 
			new ExecutionListener<Void>() {
				public void done(Void nothing) {}
				public void error(Throwable t) {
					DBRepository.this.propertyChangeSupport.firePropertyChange(
							RepositoryChangeEvent.exceptionOccurred.name(),
							null, 
							t);	
				}
			});
	}

	private void removeTagDefinition(final TagDefinition tagDefinition) {
		backgroundServiceProvider.submit(
			new DefaultProgressCallable<String>() {
				public String call() throws Exception {
					Session session = sessionFactory.openSession();
					try {
						session.beginTransaction();
						
						DBTagDefinition dbTagsetDefinition = 
							(DBTagDefinition)session.get(
								DBTagDefinition.class, 
								uuidToDBid.get(tagDefinition.getID()));
						
						session.delete(dbTagsetDefinition);
						session.getTransaction().commit();
						
						return tagDefinition.getID();
					}
					catch (Exception e) {
						try {
							if (session.getTransaction().isActive()) {
								session.getTransaction().rollback();
							}
						}
						catch(Exception notOfInterest){}
						throw new Exception(e);
					}
					finally {
						CloseSafe.close(new ClosableSession(session));
					}
				}
			}, 
			new ExecutionListener<String>() {
				public void done(String uuid) {
					uuidToDBid.remove(uuid);
				}
				public void error(Throwable t) {
					DBRepository.this.propertyChangeSupport.firePropertyChange(
							RepositoryChangeEvent.exceptionOccurred.name(),
							null, 
							t);	
				}
			});
	}
	
	private void saveTagDefinition(final TagsetDefinition tagsetDefinition,
			final TagDefinition tagDefinition) {
		addAuthorIfAbsent(tagDefinition);
		
		backgroundServiceProvider.submit(
		new DefaultProgressCallable<DBTagDefinition>() {
			public DBTagDefinition call() throws Exception {
				Session session = sessionFactory.openSession();
				
				try {
					DBTagDefinition dbTagDefinition = 
						new DBTagDefinition(
							tagDefinition.getVersion().getDate(),
							idGenerator.catmaIDToUUIDBytes(
									tagDefinition.getID()),
							tagDefinition.getName(),
							(DBTagsetDefinition)session.load(
									DBTagsetDefinition.class, 
									uuidToDBid.get(tagsetDefinition.getID())),
							(tagDefinition.getParentID()==null)? null :
								uuidToDBid.get(tagDefinition.getParentID()),
							idGenerator.catmaIDToUUIDBytes(
									tagDefinition.getParentID()));
					PropertyDefinition colorDefinition = 
						tagDefinition.getPropertyDefinitionByName(
							PropertyDefinition.SystemPropertyName.catma_displaycolor.name());
					DBPropertyDefinition dbColorDefinition = 
						new DBPropertyDefinition(
							idGenerator.catmaIDToUUIDBytes(
								colorDefinition.getID()),
							colorDefinition.getName(),
							dbTagDefinition,
							true);
					dbColorDefinition.setSingleValue(colorDefinition.getFirstValue());
					
					dbTagDefinition.getDbPropertyDefinitions().add(dbColorDefinition);
					
					session.beginTransaction();
					session.save(dbTagDefinition);
					session.getTransaction().commit();
					return dbTagDefinition;
				}
				catch (Exception e) {
					try {
						if (session.getTransaction().isActive()) {
							session.getTransaction().rollback();
						}
					}
					catch(Exception notOfInterest){}
					throw new Exception(e);
				}
				finally {
					CloseSafe.close(new ClosableSession(session));
				}
			}
		}, 
		new ExecutionListener<DBTagDefinition>() {
			public void done(DBTagDefinition result) {
				uuidToDBid.put(
						tagDefinition.getID(), result.getTagDefinitionId());
			}
			public void error(Throwable t) {
				DBRepository.this.propertyChangeSupport.firePropertyChange(
						RepositoryChangeEvent.exceptionOccurred.name(),
						null, 
						t);	
			}
		});

	}

	private void removeTagsetDefinition(final TagsetDefinition tagsetDefinition) {
		backgroundServiceProvider.submit(
			new DefaultProgressCallable<String>() {
				public String call() throws Exception {
					Session session = sessionFactory.openSession();
					try {
						session.beginTransaction();
						
						DBTagsetDefinition dbTagsetDefinition = 
							(DBTagsetDefinition)session.get(
								DBTagsetDefinition.class, 
								uuidToDBid.get(tagsetDefinition.getID()));
						
						session.delete(dbTagsetDefinition);
						session.getTransaction().commit();
						
						return tagsetDefinition.getID();
					}
					catch (Exception e) {
						try {
							if (session.getTransaction().isActive()) {
								session.getTransaction().rollback();
							}
						}
						catch(Exception notOfInterest){}
						throw new Exception(e);
					}
					finally {
						CloseSafe.close(new ClosableSession(session));
					}
				}
			}, 
			new ExecutionListener<String>() {
				public void done(String uuid) {
					uuidToDBid.remove(uuid);
				}
				public void error(Throwable t) {
					DBRepository.this.propertyChangeSupport.firePropertyChange(
							RepositoryChangeEvent.exceptionOccurred.name(),
							null, 
							t);	
				}
			});

	}

	private void saveTagsetDefinition(final ITagLibrary tagLibrary,
			final TagsetDefinition tagsetDefinition) {
		
		backgroundServiceProvider.submit(
		new DefaultProgressCallable<DBTagsetDefinition>() {
			public DBTagsetDefinition call() throws Exception {
				DBTagsetDefinition dbTagsetDefinition = 
					new DBTagsetDefinition(
						idGenerator.catmaIDToUUIDBytes(tagsetDefinition.getID()),
						tagsetDefinition.getVersion().getDate(),
						tagsetDefinition.getName(),
						((DBTagLibrary)tagLibrary).getTagLibraryId());
				
				Session session = sessionFactory.openSession();
				
				try {
					session.beginTransaction();
					session.save(dbTagsetDefinition);
					session.getTransaction().commit();
					return dbTagsetDefinition;
				}
				catch (Exception e) {
					try {
						if (session.getTransaction().isActive()) {
							session.getTransaction().rollback();
						}
					}
					catch(Exception notOfInterest){}
					throw new Exception(e);
				}
				finally {
					CloseSafe.close(new ClosableSession(session));
				}
			}
		}, 
		new ExecutionListener<DBTagsetDefinition>() {
			public void done(DBTagsetDefinition result) {
				uuidToDBid.put(
						tagsetDefinition.getID(), result.getTagsetDefinitionId());
			}
			public void error(Throwable t) {
				DBRepository.this.propertyChangeSupport.firePropertyChange(
						RepositoryChangeEvent.exceptionOccurred.name(),
						null, 
						t);	
			}
		});
	}
	
	private void updateTagsetDefinition(final TagsetDefinition tagsetDefinition) {
		backgroundServiceProvider.submit(
			new DefaultProgressCallable<Void>() {
				public Void call() throws Exception {
					Session session = sessionFactory.openSession();
					try {
						session.beginTransaction();
						
						DBTagsetDefinition dbTagsetDefinition = 
							(DBTagsetDefinition)session.load(
								DBTagsetDefinition.class, 
								uuidToDBid.get(tagsetDefinition.getID()));
						dbTagsetDefinition.setName(
								tagsetDefinition.getName());
						dbTagsetDefinition.setVersion(
							tagsetDefinition.getVersion().getDate());
						
						session.update(dbTagsetDefinition);
						
						session.getTransaction().commit();
						
						return null;
					}
					catch (Exception e) {
						try {
							if (session.getTransaction().isActive()) {
								session.getTransaction().rollback();
							}
						}
						catch(Exception notOfInterest){}
						throw new Exception(e);
					}
					finally {
						CloseSafe.close(new ClosableSession(session));
					}
				}
			}, 
			new ExecutionListener<Void>() {
				public void done(Void nothing) {}
				public void error(Throwable t) {
					DBRepository.this.propertyChangeSupport.firePropertyChange(
							RepositoryChangeEvent.exceptionOccurred.name(),
							null, 
							t);	
				}
			});
	}

	public void addPropertyChangeListener(
			RepositoryChangeEvent propertyChangeEvent,
			PropertyChangeListener propertyChangeListener) {
		this.propertyChangeSupport.addPropertyChangeListener(
				propertyChangeEvent.name(), propertyChangeListener);
	}
	
	public void removePropertyChangeListener(
			RepositoryChangeEvent propertyChangeEvent,
			PropertyChangeListener propertyChangeListener) {
		this.propertyChangeSupport.removePropertyChangeListener(
				propertyChangeEvent.name(), propertyChangeListener);
	}

	public String getName() {
		return name;
	}

	public void open(Map<String, String> userIdentification) throws Exception {
		sessionFactory = hibernateConfig.buildSessionFactory(serviceRegistry);
		
		Session session = sessionFactory.openSession();
		
		try {
			loadCurrentUser(session, userIdentification);
			loadContent(session); //TODO: consider lazy loading
		}
		catch (Exception e) {
			try {
				if (session.getTransaction().isActive()) {
					session.getTransaction().rollback();
				}
			}
			catch(Exception notOfInterest){}
			throw new Exception(e);
		}
		finally {
			CloseSafe.close(new ClosableSession(session));
		}
	}

	private void loadContent(Session session) {
		loadSourceDocuments(session);
		loadTagLibraryReferences(session);
	}

	@SuppressWarnings("unchecked")
	private void loadTagLibraryReferences(Session session) {
		if (!currentUser.isLocked()) {
			Query query = session.createQuery(
					"select tl from "
					+ DBTagLibrary.class.getSimpleName() + " as tl "
					+ " inner join tl.dbUserTagLibraries as utl "
					+ " inner join utl.dbUser as user "
					+ " where tl.independent = true and user.userId = " 
					+ currentUser.getUserId() );
			
			for (DBTagLibrary tagLibrary : (List<DBTagLibrary>)query.list()) {
				this.tagLibrariesByID.put(tagLibrary.getId(), tagLibrary);
				this.tagLibraryReferences.add(
					new TagLibraryReference(
							tagLibrary.getId(), tagLibrary.getName()));
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	private void loadSourceDocuments(Session session) {
		if (!currentUser.isLocked()) {
			Query query = 
				session.createQuery(
					"select sd from " 
					+ DBSourceDocument.class.getSimpleName() + " as sd "
					+ " inner join sd.dbUserSourceDocuments as usd "
					+ " inner join usd.dbUser as user " 
					+ " left join fetch sd.dbUserMarkupCollections as usc "
					+ " left join fetch usc.dbUserUserMarkupCollections uumc "
					+ " where user.userId = " + currentUser.getUserId());
			
			for (DBSourceDocument sd : (List<DBSourceDocument>)query.list()) {
				for (DBUserMarkupCollection dbUmc : sd.getDbUserMarkupCollections()) {
					if (dbUmc.hasAccess(currentUser)) {
						sd.addUserMarkupCollectionReference(
							new UserMarkupCollectionReference(
									dbUmc.getId(), dbUmc.getContentInfoSet()));
					}
				}
				this.sourceDocumentsByID.put(sd.getID(), sd);
			}
		}		
	}

	private void loadCurrentUser(Session session,
			Map<String, String> userIdentification) {
		
		Query query = session.createQuery(
				"from " + DBUser.class.getSimpleName() +
				" where identifier=:curIdentifier");
		query.setString(
				"curIdentifier", userIdentification.get("user.ident"));
		
		@SuppressWarnings("unchecked")
		List<DBUser> result = query.list();
		
		if (result.isEmpty()) {
			currentUser = createUser(session, userIdentification);
		}
		else {
			currentUser = result.get(0);
			if (result.size() > 1) {
				throw new IllegalStateException(
						"the repository returned more than one user " +
						"for the same identification");
			}
		}
	}

	private DBUser createUser(Session session,
			Map<String, String> userIdentification) {
		DBUser user = new DBUser(userIdentification.get("user.ident"));
		session.beginTransaction();
		session.save(user);
		session.getTransaction().commit();
		return user;
	}

	public Collection<ISourceDocument> getSourceDocuments() {
		return  Collections.unmodifiableCollection(sourceDocumentsByID.values());
	}

	public ISourceDocument getSourceDocument(String id) {
		return sourceDocumentsByID.get(id);
	}

	public Set<Corpus> getCorpora() {
		return Collections.unmodifiableSet(corpora);
	}

	public Set<TagLibraryReference> getTagLibraryReferences() {
		return Collections.unmodifiableSet(this.tagLibraryReferences);
	}

	public IUserMarkupCollection getUserMarkupCollection(
			UserMarkupCollectionReference userMarkupCollectionReference) {
		// TODO Auto-generated method stub
		return null;
	}

	public StaticMarkupCollection getStaticMarkupCollection(
			StaticMarkupCollectionReference staticMarkupCollectionReference) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITagLibrary getTagLibrary(TagLibraryReference tagLibraryReference) 
			throws IOException{
		
		DBTagLibrary tagLibrary = 
				this.tagLibrariesByID.get(tagLibraryReference.getId());
		loadTagLibrayContent(tagLibrary);
		
		return tagLibrary;
	}

	@SuppressWarnings("unchecked")
	private void loadTagLibrayContent(DBTagLibrary tagLibrary) throws IOException {

		Session session = sessionFactory.openSession();
		
		try {
			Query query = session.createQuery(
					"select tsd from "
					+ DBTagsetDefinition.class.getSimpleName() + " as tsd "
					+ "left join fetch tsd.dbTagDefinitions as td "
					+ "left join fetch td.dbPropertyDefinitions as pd "
					+ "left join fetch pd.dbPropertyDefPossibleValues "
					+ "where tsd.tagLibraryId = " + tagLibrary.getTagLibraryId());
			
			for (DBTagsetDefinition dbTsDef : (List<DBTagsetDefinition>)query.list()) {
				
				TagsetDefinition tsDef = 
						new TagsetDefinition(
							idGenerator.uuidBytesToCatmaID(dbTsDef.getUuid()),
							dbTsDef.getName(),
							new Version(dbTsDef.getVersion()));
				uuidToDBid.put(tsDef.getID(), dbTsDef.getTagsetDefinitionId());
				
				for (DBTagDefinition dbTDef : dbTsDef.getDbTagDefinitions()) {
					TagDefinition tDef = 
						new TagDefinition(
							idGenerator.uuidBytesToCatmaID(dbTDef.getUuid()),
							dbTDef.getName(),
							new Version(dbTDef.getVersion()),
							idGenerator.uuidBytesToCatmaID(dbTDef.getParentUuid()));
					tsDef.addTagDefinition(tDef);
					uuidToDBid.put(tDef.getID(), dbTDef.getTagDefinitionId());

					for (DBPropertyDefinition dbPDef : 
						dbTDef.getDbPropertyDefinitions()) {
						
						List<String> pValues = new ArrayList<String>();
						for (DBPropertyDefPossibleValue dbPVal : 
							dbPDef.getDbPropertyDefPossibleValues()) {
							pValues.add(dbPVal.getValue());
						}
						
						PropertyPossibleValueList ppvList = 
								new PropertyPossibleValueList(pValues, true);
						PropertyDefinition pDef = 
								new PropertyDefinition(
									idGenerator.uuidBytesToCatmaID(dbPDef.getUuid()),
									dbPDef.getName(),
									ppvList);
						uuidToDBid.put(pDef.getID(), dbPDef.getPropertyDefinitionId());

						if (dbPDef.isSystemproperty()) {
							tDef.addSystemPropertyDefinition(pDef);
						}
						else {
							tDef.addUserDefinedPropertyDefinition(pDef);
						}
					}
				}
				tagLibrary.add(tsDef);
			}
		}
		catch (Exception e) {
			try {
				if (session.getTransaction().isActive()) {
					session.getTransaction().rollback();
				}
			}
			catch(Exception notOfInterest){}
			throw new IOException(e);
		}
		finally {
			CloseSafe.close(new ClosableSession(session));
		}
		
	}

	public void delete(ISourceDocument sourceDocument) {
		// TODO Auto-generated method stub

	}

	public void delete(IUserMarkupCollection userMarkupCollection) {
		// TODO Auto-generated method stub

	}

	public void delete(StaticMarkupCollection staticMarkupCollection) {
		// TODO Auto-generated method stub

	}

	public void update(ISourceDocument sourceDocument) {
		// TODO Auto-generated method stub

	}

	public void update(IUserMarkupCollection userMarkupCollection,
			ISourceDocument sourceDocument) throws IOException {
		// TODO Auto-generated method stub

	}

	public void update(StaticMarkupCollection staticMarkupCollection) {
		// TODO Auto-generated method stub

	}

	public void insert(ISourceDocument sourceDocument) throws IOException {
		if (sourceDocument instanceof SourceDocument) {
			sourceDocument = 
					new DBSourceDocument((SourceDocument)sourceDocument);
		}
		Session session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			session.save(sourceDocument);
			
			DBUserSourceDocument dbUserSourceDocument = 
					new DBUserSourceDocument(
							currentUser, (DBSourceDocument)sourceDocument);
			
			session.save(dbUserSourceDocument);
			
			dbSourceDocumentHandler.insert(sourceDocument);
			
			indexer.index(sourceDocument);
			
			session.getTransaction().commit();
			
			this.sourceDocumentsByID.put(sourceDocument.getID(), sourceDocument);
			
			this.propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.sourceDocumentAdded.name(),
					null, sourceDocument.getID());
		}
		catch (Exception e) {
			try {
				if (session.getTransaction().isActive()) {
					session.getTransaction().rollback();
				}
			}
			catch(Exception notOfInterest){}
			throw new IOException(e);
		}
		finally {
			CloseSafe.close(new ClosableSession(session));
		}
	}
	
	public StaticMarkupCollectionReference insert(
			StaticMarkupCollection staticMarkupCollection) {
		// TODO Auto-generated method stub
		return null;
	}

	public void createUserMarkupCollection(String name,
			ISourceDocument sourceDocument) throws IOException {
		
		DBUserMarkupCollection dbUserMarkupCollection = 
				new DBUserMarkupCollection(
						((DBSourceDocument)sourceDocument).getSourceDocumentId(), 
						name);
		DBUserUserMarkupCollection dbUserUserMarkupCollection =
				new DBUserUserMarkupCollection(
						currentUser, dbUserMarkupCollection);
		
		Session session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			
			session.save(dbUserMarkupCollection);
			session.save(dbUserUserMarkupCollection);
			
			session.getTransaction().commit();
			
			UserMarkupCollectionReference reference = 
					new UserMarkupCollectionReference(
							dbUserMarkupCollection.getId(), 
							dbUserMarkupCollection.getContentInfoSet());
			
			this.propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.userMarkupCollectionAdded.name(),
					null, new Pair<UserMarkupCollectionReference, ISourceDocument>(
							reference,sourceDocument));

		}
		catch (Exception e) {
			try {
				if (session.getTransaction().isActive()) {
					session.getTransaction().rollback();
				}
			}
			catch(Exception notOfInterest){}
			throw new IOException(e);
		}
		finally {
			CloseSafe.close(new ClosableSession(session));
		}
	}
	
	public void createTagLibrary(String name) throws IOException {
		
		DBTagLibrary tagLibrary = new DBTagLibrary(name, true);
		DBUserTagLibrary dbUserTagLibrary = 
				new DBUserTagLibrary(currentUser, tagLibrary);
				
		Session session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			
			session.save(tagLibrary);
			session.save(dbUserTagLibrary);
			
			session.getTransaction().commit();
			tagLibrariesByID.put(tagLibrary.getId(), tagLibrary);
			
			this.propertyChangeSupport.firePropertyChange(
					RepositoryChangeEvent.tagLibraryAdded.name(),
					null, 
					new TagLibraryReference(
							tagLibrary.getId(), tagLibrary.getName()));

		}
		catch (Exception e) {
			try {
				if (session.getTransaction().isActive()) {
					session.getTransaction().rollback();
				}
			}
			catch(Exception notOfInterest){}
			throw new IOException(e);
		}
		finally {
			CloseSafe.close(new ClosableSession(session));
		}
	}

	public void importTagLibrary(InputStream inputStream) throws IOException {
		tagManagerListenersEnabled = false;

		TagLibrarySerializationHandler tagLibrarySerializationHandler = 
				serializationHandlerFactory.getTagLibrarySerializationHandler();
		
		final ITagLibrary tagLibrary = 
				tagLibrarySerializationHandler.deserialize(null, inputStream);
		
		
		backgroundServiceProvider.submit(
				new DefaultProgressCallable<DBTagLibrary>() {
			public DBTagLibrary call() throws Exception {
				DBTagLibrary dbTagLibrary = new DBTagLibrary(tagLibrary, true);
				DBUserTagLibrary dbUserTagLibrary = 
						new DBUserTagLibrary(currentUser, dbTagLibrary);
				
				Session session = sessionFactory.openSession();
				try {
					session.beginTransaction();
					
					session.save(dbTagLibrary);
					session.save(dbUserTagLibrary);
					
					Query queryForExisingTagsetDef = session.createQuery(
							"from " + DBTagsetDefinition.class.getSimpleName() +
							" where uuid = :currentUuid");
					for (TagsetDefinition tsDef : dbTagLibrary) {
						queryForExisingTagsetDef.setBinary(
								"currentUuid", 
								idGenerator.catmaIDToUUIDBytes(tsDef.getID()));
						if (!queryForExisingTagsetDef.list().isEmpty()) {
							//FIXME: deep copy the tagsetdefinition with new IDs
						}
						importTagsetDefinition(
								session, dbTagLibrary.getTagLibraryId(), 
								tsDef);
					}
					
					SQLQuery query = session.createSQLQuery(
							"UPDATE tagdefinition td1, tagdefinition td2 " +
							"SET td1.parentID = td2.tagDefinitionID " +
							"WHERE td1.parentUuid = td2.uuid AND " +
							"td1.parentID IS NULL AND " + 
							"td1.parentUuid IS NOT NULL");
					
					query.executeUpdate();
					
					session.getTransaction().commit();
					
					return dbTagLibrary;
				}
				catch (Exception e) {
					try {
						if (session.getTransaction().isActive()) {
							session.getTransaction().rollback();
						}
					}
					catch(Exception notOfInterest){}
					throw new IOException(e);
				}
				finally {
					CloseSafe.close(new ClosableSession(session));
				}
			};
		}, 
		new ExecutionListener<DBTagLibrary>() {
			public void done(DBTagLibrary result) {
				tagLibrariesByID.put(result.getId(), result);

				tagManagerListenersEnabled = true;

				DBRepository.this.propertyChangeSupport.firePropertyChange(
						RepositoryChangeEvent.tagLibraryAdded.name(),
						null, 
						new TagLibraryReference(
								result.getId(), result.getName()));
			}
			public void error(Throwable t) {
				tagManagerListenersEnabled = true;

				DBRepository.this.propertyChangeSupport.firePropertyChange(
						RepositoryChangeEvent.exceptionOccurred.name(),
						null, 
						t);				
			}
		});
	}
	
	private void importTagsetDefinition(
			Session session, Integer tagLibraryId, TagsetDefinition tsDef) {
		DBTagsetDefinition dbTagsetDefinition = 
				new DBTagsetDefinition(
						idGenerator.catmaIDToUUIDBytes(tsDef.getID()),
						tsDef.getVersion().getDate(),
						tsDef.getName(), tagLibraryId);
		for (TagDefinition tDef : tsDef) {
			DBTagDefinition dbTagDefinition = 
					createDBTagDefinition(tDef, dbTagsetDefinition);
			dbTagsetDefinition.getDbTagDefinitions().add(dbTagDefinition);
		}
		
		session.save(dbTagsetDefinition);
		
	}
	
	private void addAuthorIfAbsent(TagDefinition tDef) {		
		if (tDef.getAuthor() == null) {
			PropertyDefinition pdAuthor = 
					new PropertyDefinition(
							idGenerator.generate(), 
							PropertyDefinition.SystemPropertyName.catma_markupauthor.name(), 
							new PropertyPossibleValueList(currentUser.getIdentifier()));
			tDef.addSystemPropertyDefinition(pdAuthor);
		}
	}

	private DBTagDefinition createDBTagDefinition(
			TagDefinition tDef, 
			DBTagsetDefinition dbTagsetDefinition) {
		addAuthorIfAbsent(tDef);

		DBTagDefinition dbTagDefinition = 
				new DBTagDefinition(
					tDef.getVersion().getDate(),
					idGenerator.catmaIDToUUIDBytes(tDef.getID()),
					tDef.getName(),
					dbTagsetDefinition,
					idGenerator.catmaIDToUUIDBytes(tDef.getParentID()));
					
		for (PropertyDefinition pDef : tDef.getSystemPropertyDefinitions()) {
			DBPropertyDefinition dbPropertyDefinition = 
					createDBPropertyDefinition(
							pDef, dbTagDefinition, true);
			dbTagDefinition.getDbPropertyDefinitions().add(dbPropertyDefinition);
		}
		
		for (PropertyDefinition pDef : tDef.getUserDefinedPropertyDefinitions()) {
			DBPropertyDefinition dbPropertyDefinition = 
					createDBPropertyDefinition(
							pDef, dbTagDefinition, false);
			dbTagDefinition.getDbPropertyDefinitions().add(dbPropertyDefinition);
		}		
		return dbTagDefinition;
	}

	private DBPropertyDefinition createDBPropertyDefinition(
			PropertyDefinition pDef,
			DBTagDefinition dbTagDefinition, boolean isSystemProperty) {

		DBPropertyDefinition dbPropertyDefinition = 
				new DBPropertyDefinition(
					idGenerator.catmaIDToUUIDBytes(pDef.getID()),
					pDef.getName(),
					dbTagDefinition, isSystemProperty);
		
		for (String value : 
			pDef.getPossibleValueList().getPropertyValueList().getValues()) {
			
			DBPropertyDefPossibleValue dbPropertyDefPossibleValue =
					new DBPropertyDefPossibleValue(value, dbPropertyDefinition);
			dbPropertyDefinition.getDbPropertyDefPossibleValues().add(
					dbPropertyDefPossibleValue);
		}
		
		
		return dbPropertyDefinition;
	}

	public String getIdFromURI(URI uri) {
		return dbSourceDocumentHandler.getIDFromURI(uri);
	}
	
	public boolean isAuthenticationRequired() {
		return authenticationRequired;
	}
	
	public User getUser() {
		return currentUser;
	}
	
	public Indexer getIndexer() {
		return indexer;
	}
	
	public void close() {
		tagManager.removePropertyChangeListener(
				TagManagerEvent.tagsetDefinitionChanged,
				tagsetDefinitionChangedListener);
		tagManager.removePropertyChangeListener(
				TagManagerEvent.tagDefinitionChanged,
				tagDefinitionChangedListener);	
		try {
			indexer.close();
		}
		finally {
			sessionFactory.close();
		}
	}
}
