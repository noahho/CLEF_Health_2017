package model.wrappers;

import model.PubMedFile;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.spark.mllib.regression.LabeledPoint;

import query.QueryHelpers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class PubMedFileIndexingWrapper {

	private PubMedFile file;

	private final static Logger LOGGER = Logger
			.getLogger(PubMedFileIndexingWrapper.class.getName());

	public PubMedFileIndexingWrapper(PubMedFile file) {
		this.file = file;
	}

	public Document getIndexingDocument() throws IOException, ParseException {
		Document document = new Document();

		// index file contents
		Field contentField = new TextField("abstract",
				this.file.content.getAbstractText(), Field.Store.YES);

		Field titleField = new TextField("title", this.file.content.getTitle(),
				Field.Store.YES);
		
		Field pidField = new StringField("id",
				String.valueOf(this.file.getPID()), Field.Store.YES);
		
		Field meshHeadingsField = new TextField("meshHeadings",
				String.valueOf(this.file.content.meshHeadings), Field.Store.YES);

		Field gensimAbstractField = new StringField("gensimAbstract",
				this.file.content.gensimAbstractVector.toString(), Field.Store.YES);
		Field gensimTitleField = new StringField("gensimTitle",
				this.file.content.gensimTitleVector.toString(), Field.Store.YES);
		Field gensimMeshField = new StringField("gensimMesh",
				this.file.content.gensimMeshVector.toString(), Field.Store.YES);
		
		Field languageField = new StringField("language",
				String.valueOf(this.file.content.language), Field.Store.YES);
		
		Field sourceField = new StringField("sources",
				StringUtils.join(this.file.content.sources.toArray(), "||"), Field.Store.YES);

		Field pubYearField = new StringField("pubYear", String.valueOf(this.file.content.pubYear), Field.Store.YES);

		String temp = (this.file.content.abstractMetaMap == null) ? ""
				: this.file.content.abstractMetaMap.getTestCuiField();
		Field testAbstractCuis = new TextField("abstract_metamap_test",
				temp,
				Field.Store.YES);

		temp = (this.file.content.abstractMetaMap == null) ? ""
				: this.file.content.abstractMetaMap.getTargetCuiField();
		Field targetAbstractCuis = new TextField("abstract_metamap_target",
				temp,
				Field.Store.YES);

		temp = (this.file.content.abstractMetaMap == null) ? ""
				: this.file.content.abstractMetaMap.getGeneralCuiField();
		Field generalAbstractCuis = new TextField("abstract_metamap_general",
				temp,
				Field.Store.YES);

		temp = (this.file.content.titleMetaMap == null) ? ""
				: this.file.content.titleMetaMap.getTestCuiField();
		Field testTitleCuis = new TextField("title_metamap_test",
				temp,
				Field.Store.YES);

		temp = (this.file.content.titleMetaMap == null) ? ""
				: this.file.content.titleMetaMap.getTargetCuiField();
		Field targetTitleCuis = new TextField("title_metamap_target",
				temp,
				Field.Store.YES);

		temp = (this.file.content.titleMetaMap == null) ? ""
				: this.file.content.titleMetaMap.getGeneralCuiField();
		Field generalTitleCuis = new TextField("title_metamap_general",
				temp,
				Field.Store.YES);
		
		Field topicsRetrievedField = new TextField("topics",
				String.join(" ", this.file.retrievedFileTopicList),
				Field.Store.YES);

		document.add(contentField);
		document.add(titleField);
		document.add(pidField);
		document.add(pubYearField);
		document.add(meshHeadingsField);
		document.add(sourceField);
		document.add(languageField);
		
		document.add(topicsRetrievedField);
		
		document.add(gensimAbstractField);
		document.add(gensimMeshField);
		document.add(gensimTitleField);
		
		document.add(targetAbstractCuis);
		document.add(testAbstractCuis);
		document.add(generalAbstractCuis);
		document.add(targetTitleCuis);
		document.add(testTitleCuis);
		document.add(generalTitleCuis);
		
		return document;
	}

	public int getPid() {
		return this.file.getPID();
	}
}
