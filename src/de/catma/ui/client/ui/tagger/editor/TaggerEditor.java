/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2009-2013  University Of Hamburg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.catma.ui.client.ui.tagger.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Text;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;

import de.catma.ui.client.ui.tagger.DebugUtil;
import de.catma.ui.client.ui.tagger.editor.TaggerEditorListener.TaggerEditorEventType;
import de.catma.ui.client.ui.tagger.impl.SelectionHandlerImplStandard;
import de.catma.ui.client.ui.tagger.impl.SelectionHandlerImplStandard.Range;
import de.catma.ui.client.ui.tagger.shared.ClientTagDefinition;
import de.catma.ui.client.ui.tagger.shared.ClientTagInstance;
import de.catma.ui.client.ui.tagger.shared.ContentElementID;
import de.catma.ui.client.ui.tagger.shared.TextRange;

public class TaggerEditor extends FocusWidget 
	implements MouseUpHandler, FocusHandler, BlurHandler,
		MouseDownHandler, KeyUpHandler, ClickHandler {
	private static final String LINEID_PREFIX = "LINE.";
	private static Logger logger = Logger.getLogger(TaggerEditor.class.getName());
	private static SelectionHandlerImplStandard impl = 
			 GWT.create(SelectionHandlerImplStandard.class);

	private List<Range> lastRangeList; 
	private List<NodeRange> lastTextRanges;

	private TaggerEditorListener taggerEditorListener;

	private String taggerID;
	
	private int lastClientX;
	private int lastClientY;

	private boolean highlightSelections = true;
	private boolean traceSelection = false;
	
	private String lastTagInstancePartID = null;
	private int lineCount;
	private HashMap<String,Line> lineIdToLineMap;
	
	public TaggerEditor(TaggerEditorListener taggerEditorListener) {
		super(Document.get().createDivElement());
		getElement().setTabIndex(0); // some browsers need the tabindex to fire blur/focus events
		
		this.taggerEditorListener = taggerEditorListener;
		
		// Tell GWT the events we are interested in consuming
		sinkEvents(Event.ONMOUSEUP | Event.ONMOUSEDOWN | Event.ONKEYUP | Event.ONCLICK | Event.ONBLUR | Event.ONFOCUS);

		addMouseUpHandler(this);
		addMouseDownHandler(this);
		addKeyUpHandler(this);
		addClickHandler(this);
		addBlurHandler(this);
		addFocusHandler(this);
	}
	
	/**
	 * Can be called by external dialog and or panels to remove a tagInstance with a client side event.
	 * @param tagInstanceID the tagInstance to remove
	 */
	public void removeTagInstance(String tagInstanceID) {
		removeTagInstance(tagInstanceID, true);
	}
	
	public void removeTagInstance(String tagInstanceID, boolean reportToServer) {
		int currentPartID = 1;
		Element taggedSpan = Document.get().getElementById(tagInstanceID + "_" + currentPartID++);
		while(taggedSpan != null) {
			Element parent = taggedSpan.getParentElement();
			DebugUtil.printNode(taggedSpan);
			NodeList<Node> children = taggedSpan.getChildNodes();
			for (int i=0; i<children.getLength(); i++) {
				Node child = children.getItem(i);
				parent.insertBefore(child.cloneNode(true), taggedSpan);
			}
			parent.removeChild(taggedSpan);
			taggedSpan = Document.get().getElementById(tagInstanceID + "_" + currentPartID++);
		}
		lastTagInstancePartID = null;
		
		taggerEditorListener.tagChanged(
				TaggerEditorEventType.REMOVE, tagInstanceID, reportToServer);
	}

	public void onMouseUp(MouseUpEvent event) {
		lastRangeList = impl.getRangeList();
		logger.info("Ranges: " + lastRangeList.size());
		showTagInstancesFromSelection();
	}

 	public void setHTML(HTML pageHtmlContent, int lineCount) {
		if (getElement().hasChildNodes()) {
			NodeList<Node> children = getElement().getChildNodes();
			for (int i=0; i<children.getLength();i++) {
				getElement().removeChild(children.getItem(i));
			}
		}
		getElement().appendChild(pageHtmlContent.getElement());
		this.lineCount = lineCount;
		createLineModels();
	}
	 
	private void createLineModels() {
		lineIdToLineMap = new HashMap<>();
		for (int lineId=0; lineId<this.lineCount; lineId++) {
			Element lineElement = DOM.getElementById(LINEID_PREFIX+lineId);
			LineNodeToLineConverter lineNodeToLineConverter = new LineNodeToLineConverter(lineElement);
			Line line = lineNodeToLineConverter.getLine();
			lineIdToLineMap.put(LINEID_PREFIX+lineId, line);
		}
	}

	public void createAndAddTagIntance(ClientTagDefinition tagDefinition) {
		String tagInstanceId = IDGenerator.generate();
		List<TextRange> ranges = new ArrayList<>();
		
		for (Line line : lineIdToLineMap.values())  {
			if (line.hasSelectedTextRanges()) {
				ranges.addAll(line.getSelectedTextRanges());
				line.addTagInstance(new ClientTagInstance(
					tagDefinition.getId(), 
					tagInstanceId, 
					tagDefinition.getColor(), 
					new ArrayList<>(line.getSelectedTextRanges())));
			}
		}
		
		if (!ranges.isEmpty()) {
			ClientTagInstance ti = 
					new ClientTagInstance(
							tagDefinition.getId(),
							tagInstanceId, 
							tagDefinition.getColor(), 
							ranges);
			taggerEditorListener.tagChanged(TaggerEditorEventType.ADD, ti);
		}
	}
	
	private List<NodeRange> getLastNodeRanges() {
		
		if (lastTextRanges != null) {
			return lastTextRanges;
		}
		
		ArrayList<NodeRange> nodeRanges = new ArrayList<NodeRange>();
		for (Range range : lastRangeList) { 
			if (!range.getStartNode().equals(getRootNode())
						&& !range.getEndNode().equals(getRootNode())) {
				
				Node startNode = range.getStartNode();
				int startOffset = range.getStartOffset();
				Node endNode = range.getEndNode();
				int endOffset = range.getEndOffset();

				if (startNode.getNodeType() != Element.TEXT_NODE) {
					startNode = LeafFinder.getFirstTextLeaf(startNode);
					startOffset = 0;
				}
				
				if (endNode.getNodeType() != Element.TEXT_NODE) {
					endNode = LeafFinder.getFirstTextLeaf(endNode);
					endOffset = Text.as(endNode).getLength();
				}
				
				NodeRange nodeRange = new NodeRange(startNode, startOffset, 
						endNode, endOffset);
				if (nodeRange.isPoint()) {
					//TODO: consider tagging points (needs different visualization)
					logger.info(
							"won't tag range " + nodeRange + " because it is a point");
				}
				else {
					logger.info("adding range " + nodeRange );
					nodeRanges.add(nodeRange);
				}
			}
			else {
				// one of the nodes is the root node, which is an out-of-bounds-selection
				// but we try to recover from that by looking for the next adjacent
				// text nodes
				Node startNode = range.getStartNode();
				int startOffset = range.getStartOffset();
				Node endNode = range.getEndNode();
				int endOffset = range.getEndOffset();

				if (range.getStartNode().equals(getRootNode())) {
					logger.info("startNode is root!");
					startNode = LeafFinder.getFirstTextLeaf(getRootNode().getChild(range.getStartOffset()));
					startOffset = 0;
					logger.info(
						"Setting new startNode with Offset 0: " + startNode );
				}
				
				if (range.getEndNode().equals(getRootNode())) {
					logger.info("endNode is root!");
					endNode =  LeafFinder.getFirstTextLeaf(getRootNode().getChild(range.getEndOffset()-1));
					endOffset = endNode.getNodeValue().length();
					logger.info(
						"Setting new endNode with Offset " 
								+ endOffset + ": " + endNode );
				}
				
				NodeRange nodeRange = new NodeRange(startNode, startOffset, 
						endNode, endOffset);

				if (!nodeRange.isPoint()) {
					logger.info("converted and adding range " + nodeRange);
					nodeRanges.add(nodeRange);
				}
				else {
					logger.info(
						"won't tag range " + nodeRange + " because it is a point");
				}
			}
		}

		return nodeRanges;
	}

	//TODO: better validate browser ranges
	private TextRange validateTextRange(TextRange textRange) {
		if (textRange.getStartPos() > textRange.getEndPos()) {
			taggerEditorListener.logEvent(
				"got twisted range: " + textRange 
				+ " recovering by exchanging positions!");
			return new TextRange(textRange.getEndPos(), textRange.getStartPos());
		}
		return textRange;
	}
	
	private Line getLine(Element lineElement) {
		return lineIdToLineMap.get(lineElement.getAttribute("id"));
	}

	private Element getLineElementFromDisplayLayerContentNode(Node node) {
		Element displayLayerContent = node.getParentElement().getParentElement();
		if (displayLayerContent.hasClassName("tagger-display-layer")) {
			return displayLayerContent.getParentElement().getParentElement();
		}
		return null;
	}
	
	public boolean hasSelection() {
		logger.info("checking for selection");
		if ((lastTextRanges != null) && !lastTextRanges.isEmpty()) {
			logger.info("found lastTextRanges: " + lastTextRanges.size());
			return true;
		}
		
		if ((lastRangeList != null) && !lastRangeList.isEmpty()) {
			logger.info("found lastRangeList: " + lastRangeList.size());
			for (Range r : lastRangeList) {
				if ((r.getEndNode()!=r.getStartNode()) 
						|| (r.getEndOffset() != r.getStartOffset())) {
					logger.info("found at least one range: " + r);
					return true;
				}
			}
			logger.info("lastRangeList contains only a point");
		}
		
		return false;
	}
	
	public String getTagInstanceID(String tagInstancePartID) {
		return ClientTagInstance.getTagInstanceIDFromPartId(tagInstancePartID);
	}

	public void setTaggerID(String taggerID) {
		logger.info("Setting taggerID: " + taggerID);
		this.taggerID = taggerID;
	}
	
	public String getTaggerID() {
		return taggerID;
	}
	
	private Element getRootNode() {
		return Document.get().getElementById(
				ContentElementID.CONTENT.name() + taggerID);
	}

	private void showTagInstancesFromSelection() {
		if (hasSelection()) {
			List<NodeRange> lastNodeRanges = getLastNodeRanges();
			HashSet<String> tagInstanceIDs = new HashSet<>();
			
			for (NodeRange nodeRange : lastNodeRanges) {
				Node startNode = nodeRange.getStartNode();
				int startOffset = nodeRange.getStartOffset();
				
				Node endNode = nodeRange.getEndNode();
				int endOffset = nodeRange.getEndOffset();		
				
				if (startNode.equals(endNode)) {
					Element lineElement = getLineElementFromDisplayLayerContentNode(startNode);
					Line line = getLine(lineElement);
					
					TextRange textRange = new TextRange(line.getLineOffset()+startOffset, line.getLineOffset()+endOffset);
					tagInstanceIDs.addAll(line.getTagInstanceIDs(textRange));
				}
				else {
					Element startLineElement = getLineElementFromDisplayLayerContentNode(startNode);
					Element endLineElement = getLineElementFromDisplayLayerContentNode(endNode);
					Line startLine = getLine(startLineElement);
					Line endLine = getLine(endLineElement);

					TextRange startRange = 
						new TextRange(startLine.getLineOffset()+startOffset, startLine.getTextRange().getEndPos());
					tagInstanceIDs.addAll(startLine.getTagInstanceIDs(startRange));
					
					TextRange endRange = 
						new TextRange(endLine.getTextRange().getStartPos(), endLine.getLineOffset()+endOffset);
					tagInstanceIDs.addAll(endLine.getTagInstanceIDs(endRange));
					
					for (int lineId = startLine.getLineId()+1; lineId<endLine.getLineId(); lineId++) {
						Element lineElement = DOM.getElementById(LINEID_PREFIX+lineId);
						Line line = getLine(lineElement);
						tagInstanceIDs.addAll(line.getTagInstanceIDs(line.getTextRange()));
					}
				}
			}
			
			if (!tagInstanceIDs.isEmpty()) {
				taggerEditorListener.tagsSelected(tagInstanceIDs);
			}
		}
	}
	
	private void highlightSelection() {
		if (highlightSelections && hasSelection()) {
			
			List<NodeRange> lastNodeRanges = getLastNodeRanges();

			Set<Line> modifiedLines = new HashSet<>();

			for (NodeRange nodeRange : lastNodeRanges) {
				Node startNode = nodeRange.getStartNode();
				int startOffset = nodeRange.getStartOffset();
				
				Node endNode = nodeRange.getEndNode();
				int endOffset = nodeRange.getEndOffset();		
				
				if (startNode.equals(endNode)) {
					Element lineElement = getLineElementFromDisplayLayerContentNode(startNode);
					Line line = getLine(lineElement);
					modifiedLines.add(line);
					
					TextRange textRange =
						new TextRange(
							line.getLineOffset()+startOffset, line.getLineOffset()+endOffset);
					
					if (!traceSelection) {
						line.removeSelectedTextRanges();
					}
					line.addSelectedTextRange(textRange);
					line.updateLineElement();
				}
				else {
					Element startLineElement = getLineElementFromDisplayLayerContentNode(startNode);
					Element endLineElement = getLineElementFromDisplayLayerContentNode(endNode);
					Line startLine = getLine(startLineElement);
					modifiedLines.add(startLine);
					Line endLine = getLine(endLineElement);
					modifiedLines.add(endLine);

					TextRange startRange = 
						new TextRange(startLine.getLineOffset()+startOffset, startLine.getTextRange().getEndPos());
					if (!traceSelection) {
						startLine.removeSelectedTextRanges();
					}
					startLine.addSelectedTextRange(startRange);
					startLine.updateLineElement();
					
					TextRange endRange = 
						new TextRange(endLine.getLineOffset(), endLine.getLineOffset() + endOffset);
					if (!traceSelection) {
						endLine.removeSelectedTextRanges();
					}
					endLine.addSelectedTextRange(endRange);
					endLine.updateLineElement();
					
					for (int lineId = startLine.getLineId()+1; lineId<endLine.getLineId(); lineId++) {
						Element lineElement = DOM.getElementById(LINEID_PREFIX+lineId);
						Line line = getLine(lineElement);
						modifiedLines.add(line);
						if (!traceSelection) {
							line.removeSelectedTextRanges();
						}
						line.addSelectedTextRange(line.getTextRange());
						line.updateLineElement();
					}
				}
			}
			
			if (!traceSelection) {
				for (Line line : lineIdToLineMap.values()) {
					if (!modifiedLines.contains(line) && line.hasSelectedTextRanges()) {
						line.removeSelectedTextRanges();
						line.updateLineElement();
					}
				}
			}
		}
	}
	
	@Override
	public void onBlur(BlurEvent event) {
		if (!traceSelection) {
			highlightSelection();
		}
	}

	public void onFocus(FocusEvent event) {
		for (Line line : lineIdToLineMap.values()) {
			if (line.hasSelectedTextRanges()) {
				line.removeSelectedTextRanges();
				line.updateLineElement();
			}
		}
		lastTextRanges = null;
	}
	
	public void onKeyUp(KeyUpEvent event) {
		lastRangeList = impl.getRangeList();
		logger.info("Ranges: " + lastRangeList.size());

		if (!event.isShiftKeyDown() && traceSelection) {
			highlightSelection();
		}
	}

	public void onMouseDown(MouseDownEvent event) {
		lastClientX = event.getClientX();
		lastClientY = event.getClientY();
		logger.info("mouse down at: " + lastClientX + "," + lastClientY);
	}
	

	private void fireTagsSelected(Element targetElement) {

		if (targetElement.getParentElement().hasClassName("annotation-layer")) {
			String tagInstancePartId = targetElement.getAttribute("id");
			if (tagInstancePartId.isEmpty() && (lastTagInstancePartID == null)) {
				return; // no annotation present 
			}
			String tagInstanceId = getTagInstanceID(tagInstancePartId);
			
			setTagInstanceSelected(tagInstanceId);
			
			String lineID = getLineIDFromAnnotationLayerSegment(targetElement);
			
			if (!tagInstanceId.isEmpty()) {
				if ((lastTagInstancePartID == null) 
						|| ( ! lastTagInstancePartID.equals(tagInstancePartId))) {
					this.lastTagInstancePartID = tagInstancePartId;
					logger.info("fireTagsSelected: notifying listeners");
					taggerEditorListener.tagSelected(tagInstancePartId, lineID);
				}
			}
			else {
				lastTagInstancePartID = null;
			}
		}
	}
	
	private Integer getRedFromCssRgb(String cssColor) {
		RegExp c = RegExp.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
	    MatchResult m = c.exec(cssColor);

	    if (m != null) { 
	    	return Integer.valueOf(m.getGroup(1)); 
	    }
	
		return 0;
	}
	
	private String getLineIDFromAnnotationLayerSegment(Element targetElement) {
		
		Element annotationLayer = targetElement.getParentElement();
		Element lineElement = annotationLayer.getParentElement().getParentElement();
		
		return lineElement.getAttribute("id");
	}
	
	@Override
	public void onClick(ClickEvent event) {
		if (traceSelection) {
			highlightSelection();
		}
		
		EventTarget eventTarget = event.getNativeEvent().getEventTarget();
		if (Element.is(eventTarget)) {
			Element targetElement = Element.as(eventTarget);
			fireTagsSelected(targetElement);
		}
	}

	public void setTagInstanceSelected(String tagInstanceId) {
		Element rootElement = getRootNode();
		
		NodeList<Element> tagPartElements = rootElement.getElementsByTagName("td");

		for (int index = 0; index<=tagPartElements.getLength(); index++) {
			Element tagPartElement = tagPartElements.getItem(index);
			if (tagPartElement != null) {
				if (!tagInstanceId.isEmpty()
					&& tagPartElement.hasAttribute("id") 
					&& tagPartElement.getAttribute("id").startsWith(tagInstanceId)) {
					
					String cssRgbColor = tagPartElement.getStyle().getColor();
					int red = getRedFromCssRgb(cssRgbColor);
					if (red > 170) {
						tagPartElement.addClassName("selected-tag-instance-black");
					}
					else {
						tagPartElement.addClassName("selected-tag-instance-red");
					}
					tagPartElement.removeClassName("unselected-tag-instance");
				}
				else if (tagPartElement.hasClassName("selected-tag-instance-black")) {
					tagPartElement.removeClassName("selected-tag-instance-black");
					tagPartElement.addClassName("unselected-tag-instance");
				}
				else if (tagPartElement.hasClassName("selected-tag-instance-red")) {
					tagPartElement.removeClassName("selected-tag-instance-red");
					tagPartElement.addClassName("unselected-tag-instance");
				}

			}
		}
	}

	public void setTraceSelection(boolean traceSelection) {
		this.traceSelection = traceSelection;
	}

	public void removeHighlights() {
		for (Line line : lineIdToLineMap.values()) {
			if (line.hasHighlightedTextRanges()) {
				line.removeHighlightedTextRanges();
				line.updateLineElement();
			}
		}
		
	}
}

