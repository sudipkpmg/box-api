package gov.tdhs.ecm.api;

import com.box.sdk.*;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import gov.tdhs.ecm.AppProperties;
import gov.tdhs.ecm.EcmApiApp;
import gov.tdhs.ecm.model.CitizenMetadata;
import gov.tdhs.ecm.model.DocumentMetadata;
import gov.tdhs.ecm.model.FileInfo;
import gov.tdhs.ecm.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sun.security.krb5.internal.MethodData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class SearchApiDelegateImpl implements SearchApiDelegate {

    private static Logger logger = LoggerFactory.getLogger(SearchApiDelegateImpl.class);

    @Autowired
    private AppProperties appProperties;

    @Override
    public ResponseEntity<List<FileInfo>> searchPost(Query query) {
        logger.info(query.toString());
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
            switch (query.getSearchType()) {
                case FOLDER: {
                    String folderId = query.getSearchCondition();
                    try {
                        BoxFolder folder = new BoxFolder(api, folderId);
                        Metadata folderMetadata = folder.getMetadata();
                        logger.info(folderMetadata.toString());
                        String folderMetadata_template_id = folderMetadata.getID();
                        logger.info("ID of Folder Metadata Template = {}", folderMetadata_template_id);
                        List<FileInfo> files = new ArrayList<>();
                        for (BoxItem.Info itemInfo : folder.getChildren()) {
                            if (itemInfo instanceof BoxFile.Info) {
                                FileInfo fileInfo = new FileInfo();
                                BoxFile.Info boxFileInfo = (BoxFile.Info) itemInfo;
                                String fileId = boxFileInfo.getID();
                                fileInfo.setFileId(fileId);
                                CitizenMetadata citizenMetadata = new CitizenMetadata();
                                citizenMetadata.setFirstName(folderMetadata.getString("/FirstName"));
                                citizenMetadata.setLastName(folderMetadata.getString("/LastName"));
                                citizenMetadata.setSsn4(folderMetadata.getString("/last4ofssn"));
                                citizenMetadata.setDob(LocalDate.parse(folderMetadata.getString("/DOB")));
                                fileInfo.setCitizenMetadata(citizenMetadata);
                                BoxFile boxFile = new BoxFile(api, fileId);
                                List<DocumentMetadata> documentMetadataList = new ArrayList<>();
                                Metadata fileMetadata = boxFile.getMetadata();
                                String associationStr = fileMetadata.getValue("/AssociationMetadata").asString();
                                JsonArray associationList = JsonArray.readFrom(associationStr);
                                Iterator<JsonValue> iterator = associationList.iterator();
                                while (iterator.hasNext()) {
                                    JsonObject jsonObject = iterator.next().asObject();
                                    DocumentMetadata documentMetadata = new DocumentMetadata();
                                    documentMetadata.setCaseId(jsonObject.get("caseId").asString());
                                    documentMetadata.setProgramId(jsonObject.get("programId").asString());
                                    documentMetadataList.add(documentMetadata);
                                }
                                fileInfo.setDocumentMetadataList(documentMetadataList);
                                files.add(fileInfo);
                            }
                        }
                        return new ResponseEntity(files, HttpStatus.OK);
                    } catch (BoxAPIException e) {
                        return new ResponseEntity(null, HttpStatus.CONFLICT);
                    }
                }
                case FILE: {
                    String fileId = query.getSearchCondition();
                    try {
                        List<FileInfo> files = new ArrayList<>();
                        FileInfo fileInfo = new FileInfo();
                        BoxFile boxFile = new BoxFile(api, fileId);
                        BoxFile.Info boxFileInfo = (BoxFile.Info) boxFile.getInfo();
                        String parentId = boxFileInfo.getParent().getID();
                        BoxFolder parentFolder = new BoxFolder(api, parentId);
                        Metadata folderMetadata = parentFolder.getMetadata();
                        fileInfo.setFileId(fileId);
                        CitizenMetadata citizenMetadata = new CitizenMetadata();
                        citizenMetadata.setFirstName(folderMetadata.getString("/FirstName"));
                        citizenMetadata.setLastName(folderMetadata.getString("/LastName"));
                        citizenMetadata.setSsn4(folderMetadata.getString("/last4ofssn"));
                        citizenMetadata.setDob(LocalDate.parse(folderMetadata.getString("/DOB")));
                        fileInfo.setCitizenMetadata(citizenMetadata);
                        List<DocumentMetadata> documentMetadataList = new ArrayList<>();
                        Metadata fileMetadata = boxFile.getMetadata();
                        String associationStr = fileMetadata.getValue("/AssociationMetadata").asString();
                        JsonArray associationList = JsonArray.readFrom(associationStr);
                        Iterator<JsonValue> iterator = associationList.iterator();
                        while (iterator.hasNext()) {
                            JsonObject jsonObject = iterator.next().asObject();
                            DocumentMetadata documentMetadata = new DocumentMetadata();
                            documentMetadata.setCaseId(jsonObject.get("caseId").asString());
                            documentMetadata.setProgramId(jsonObject.get("programId").asString());
                            documentMetadataList.add(documentMetadata);
                        }
                        fileInfo.setDocumentMetadataList(documentMetadataList);
                        files.add(fileInfo);
                        return new ResponseEntity(files, HttpStatus.OK);
                    } catch (BoxAPIException e) {
                        return new ResponseEntity(null, HttpStatus.CONFLICT);
                    }
                }
            }
        } catch (Exception ex) {
            return new ResponseEntity(null, HttpStatus.CONFLICT);
        }
        return new ResponseEntity(null, HttpStatus.CONFLICT);
    }

}
