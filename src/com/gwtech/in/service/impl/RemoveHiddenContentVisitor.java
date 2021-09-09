package com.gwtech.in.service.impl;


import com.aspose.words.Cell;
import com.aspose.words.Comment;
import com.aspose.words.DocumentVisitor;
import com.aspose.words.FieldEnd;
import com.aspose.words.FieldSeparator;
import com.aspose.words.FieldStart;
import com.aspose.words.Footnote;
import com.aspose.words.FormField;
import com.aspose.words.GroupShape;
import com.aspose.words.Inline;
import com.aspose.words.InlineStory;
import com.aspose.words.Node;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.Row;
import com.aspose.words.Run;
import com.aspose.words.Shape;
import com.aspose.words.ShapeBase;
import com.aspose.words.SpecialChar;
import com.aspose.words.Table;
import com.aspose.words.VisitorAction;
import com.gwtech.in.utils.Constants;

/**
 * This class when executed will remove all hidden content from the Document.
 * Implemented as a Visitor.
 */
	public class RemoveHiddenContentVisitor extends DocumentVisitor {
	/**
	 * Called when a FieldStart node is encountered in the document.
	 */
	public int visitFieldStart(final FieldStart fieldStart) throws Exception {
		// If this node is hidden, then remove it.
		if (isHidden(fieldStart)) {
			fieldStart.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a FieldEnd node is encountered in the document.
	 */
	public int visitFieldEnd(final FieldEnd fieldEnd) throws Exception {
		if (isHidden(fieldEnd)) {
			fieldEnd.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a FieldSeparator node is encountered in the document.
	 */
	public int visitFieldSeparator(final FieldSeparator fieldSeparator) throws Exception {
		if (isHidden(fieldSeparator)) {
			fieldSeparator.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a Run node is encountered in the document.
	 */
	public int visitRun(final Run run) throws Exception {
		if (isHidden(run)) {
			run.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a Paragraph node is encountered in the document.
	 */
	public int visitParagraphStart(final Paragraph paragraph) throws Exception {
		if (isHidden(paragraph)) {
			paragraph.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a FormField is encountered in the document.
	 */
	public int visitFormField(final FormField field) throws Exception {
		if (isHidden(field)) {
			field.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a GroupShape is encountered in the document.
	 */
	public int visitGroupShapeStart(final GroupShape groupShape) throws Exception {
		if (isHidden(groupShape)) {
			groupShape.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a Shape is encountered in the document.
	 */
	public int visitShapeStart(final Shape shape) throws Exception {
		if (isHidden(shape)) {
			shape.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a Comment is encountered in the document.
	 */
	public int visitCommentStart(final Comment comment) throws Exception {
		if (isHidden(comment)) {
			comment.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a Footnote is encountered in the document.
	 */
	public int visitFootnoteStart(final Footnote footnote) throws Exception {
		if (isHidden(footnote)) {
			if (Constants.taSettingForUser.isRemoveHeadersFooters())
				footnote.remove();
		}
		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when visiting of a Table node is ended in the document.
	 */
	public int visitTableEnd(final Table table) {
		// At the moment there is no way to tell if a particular Table/Row/Cell is
		// hidden.
		// Instead, if the content of a table is hidden, then all inline child nodes of
		// the table should be
		// hidden and thus removed by previous visits as well. This will result in the
		// container being empty
		// so if this is the case we know to remove the table node.
		//
		// Note that a table which is not hidden but simply has no content will not be
		// affected by this algorthim,
		// as technically they are not completely empty (for example a properly formed
		// Cell will have at least
		// an empty paragraph in it)
		if (!table.hasChildNodes()) {
			table.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when visiting of a Cell node is ended in the document.
	 */
	public int visitCellEnd(final Cell cell) {
		if (!cell.hasChildNodes() && cell.getParentNode() != null) {
			cell.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when visiting of a Row node is ended in the document.
	 */
	public int visitRowEnd(final Row row) {
		if (!row.hasChildNodes() && row.getParentNode() != null) {
			row.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Called when a SpecialCharacter is encountered in the document.
	 */
	public int visitSpecialChar(final SpecialChar character) throws Exception {
		if (isHidden(character)) {
			character.remove();
		}

		return VisitorAction.CONTINUE;
	}

	/**
	 * Returns true if the node passed is set as hidden, returns false if it is
	 * visible.
	 */
	private boolean isHidden(final Node node) {
		if (node instanceof Inline) {
			// If the node is Inline then cast it to retrieve the Font property which
			// contains the hidden property
			Inline currentNode = (Inline) node;
			return currentNode.getFont().getHidden();
		} else if (node.getNodeType() == NodeType.PARAGRAPH) {
			// If the node is a paragraph cast it to retrieve the ParagraphBreakFont which
			// contains the hidden property
			Paragraph para = (Paragraph) node;
			return para.getParagraphBreakFont().getHidden();
		} else if (node instanceof ShapeBase) {
			// Node is a shape or groupshape.
			ShapeBase shape = (ShapeBase) node;
			return shape.getFont().getHidden();
		} else if (node instanceof InlineStory) {
			// Node is a comment or footnote.
			InlineStory inlineStory = (InlineStory) node;
			return inlineStory.getFont().getHidden();
		}

		// A node that is passed to this method which does not contain a hidden property
		// will end up here.
		// By default nodes are not hidden so return false.
		return false;
	}
}