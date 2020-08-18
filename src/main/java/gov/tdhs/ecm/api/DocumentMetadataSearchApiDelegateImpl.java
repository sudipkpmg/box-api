package gov.tdhs.ecm.api;

import gov.tdhs.ecm.AppProperties;
import gov.tdhs.ecm.model.DocumentMetadataQuery;
import gov.tdhs.ecm.model.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentMetadataSearchApiDelegateImpl implements DocumentMetadataSearchApiDelegate {

    private static Logger logger = LoggerFactory.getLogger(DocumentMetadataSearchApiDelegateImpl.class);

    @Autowired
    AppProperties appProperties;

    @Override
    public ResponseEntity<List<FileInfo>> documentMetadataSearchPost(DocumentMetadataQuery documentMetadataQuery) {
        logger.info(documentMetadataQuery.toString());
        return null;
    }

}
