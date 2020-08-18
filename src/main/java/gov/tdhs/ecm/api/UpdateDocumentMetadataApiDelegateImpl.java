package gov.tdhs.ecm.api;

import com.box.sdk.*;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import gov.tdhs.ecm.AppProperties;
import gov.tdhs.ecm.model.DocumentMetadata;
import gov.tdhs.ecm.model.DocumentMetadataUpdateRequest;
import gov.tdhs.ecm.model.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class UpdateDocumentMetadataApiDelegateImpl implements UpdateDocumentMetadataApiDelegate {

    private static Logger logger = LoggerFactory.getLogger(SearchApiDelegateImpl.class);

    @Autowired
    private AppProperties appProperties;

    @Override
    public ResponseEntity<FileInfo> updateDocumentMetadataPost(DocumentMetadataUpdateRequest documentMetadataUpdateRequest) {
        logger.info(documentMetadataUpdateRequest.toString());
        String fileId = documentMetadataUpdateRequest.getFileId();
        String programId = documentMetadataUpdateRequest.getMetadata().getProgramId();
        String caseId = documentMetadataUpdateRequest.getMetadata().getCaseId();
        try {
            String clientId = appProperties.getClientID();
            String clientSecret = appProperties.getClientSecret();
            String enterpriseID = appProperties.getEnterpriseID();
            String publicKeyID = appProperties.getPublicKeyID();
            String privateKey = appProperties.getPrivateKey();
            String passphrase = appProperties.getPassphrase();
            BoxConfig boxConfig = new BoxConfig(
                    clientId,
                    clientSecret,
                    enterpriseID,
                    publicKeyID,
                    privateKey,
                    passphrase
            );
            BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
            api.asUser(appProperties.getDownloadOneUserID());
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileId(fileId);
            BoxFile boxFile = new BoxFile(api, fileId);
            Metadata fileMetadata = boxFile.getMetadata();
            String associationStr = fileMetadata.getValue("/AssociationMetadata").asString();
            JsonArray associationList = JsonArray.readFrom(associationStr);
            final JsonObject jsonObject = new JsonObject();
            jsonObject.add("programId", programId);
            jsonObject.add("caseId", caseId);
            associationList.add(jsonObject);
            final JsonObject associationObj = new JsonObject();
            String associatonListStr = associationList.toString();
            associationObj.add("AssociationMetadata", associatonListStr);
            boxFile.deleteMetadata();
            Metadata metadata = new Metadata(associationObj);
            boxFile.createMetadata(metadata);
            List<DocumentMetadata> documentMetadataList = new ArrayList<>();
            Iterator<JsonValue> iterator = associationList.iterator();
            while (iterator.hasNext()) {
                JsonObject associationObject = iterator.next().asObject();
                DocumentMetadata documentMetadata = new DocumentMetadata();
                documentMetadata.setCaseId(associationObject.get("caseId").asString());
                documentMetadata.setProgramId(associationObject.get("programId").asString());
                documentMetadataList.add(documentMetadata);
            }
            fileInfo.setDocumentMetadataList(documentMetadataList);
            return new ResponseEntity(fileInfo, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity(null, HttpStatus.CONFLICT);
        }
    }

}
