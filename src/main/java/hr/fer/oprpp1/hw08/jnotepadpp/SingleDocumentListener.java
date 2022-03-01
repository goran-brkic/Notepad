package hr.fer.oprpp1.hw08.jnotepadpp;

public interface SingleDocumentListener {
	void documentModifyStatusUpdated(SingleDocumentModel model);
	void documentFilePathUpdated(SingleDocumentModel model);
}
