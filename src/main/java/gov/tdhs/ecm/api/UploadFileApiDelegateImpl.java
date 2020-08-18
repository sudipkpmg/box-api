package gov.tdhs.ecm.api;

import com.box.sdk.*;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import gov.tdhs.ecm.AppProperties;
import gov.tdhs.ecm.EcmApiApp;
import gov.tdhs.ecm.model.UploadFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class UploadFileApiDelegateImpl implements UploadFileApiDelegate {

    private static Logger logger = LoggerFactory.getLogger(UploadFileApiDelegateImpl.class);

    @Autowired
    private AppProperties appProperties;

    @Override
    public ResponseEntity<UploadFileResponse> uploadFilePost(
            MultipartFile file,
            String boxFolderId,
            String programId,
            String caseId,
            Object documentTypeMetadata,
            Object confidentialityMetadata
    ) {

        logger.info(file.getName() + "," + boxFolderId);

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

            BoxFolder parentFolder = new BoxFolder(api, boxFolderId);
            BoxFolder.Info info = parentFolder.getInfo();
            String parentId = info.getID();

            String fileId = "No File Created";

            try {
                InputStream inputStream = file.getInputStream();
                BoxFile.Info newFileInfo = parentFolder.uploadFile(inputStream, programId);
                inputStream.close();
                fileId = newFileInfo.getID();
                BoxFile boxFile = new BoxFile(api, fileId);
                final JsonObject jsonObject = new JsonObject();
                jsonObject.add("programId", programId);
                jsonObject.add("caseId", caseId);
                JsonArray associationList = new JsonArray();
                associationList.add(jsonObject);
                final JsonObject associationObj = new JsonObject();
                String associatonListStr = associationList.toString();
                associationObj.add("AssociationMetadata", associatonListStr);
                Metadata metadata = new Metadata(associationObj);
                boxFile.createMetadata(metadata);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new Exception();
            }
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setDocumentId(fileId);
            return new ResponseEntity<>(uploadFileResponse, HttpStatus.OK);

        } catch (BoxAPIException be) {
            if (be.getResponseCode() == 409) {
                UploadFileResponse uploadFileResponse = new UploadFileResponse();
                uploadFileResponse.setDocumentId("File already exists!");
                return new ResponseEntity<>(uploadFileResponse, HttpStatus.CONFLICT);
            }
            else {
                UploadFileResponse uploadFileResponse = new UploadFileResponse();
                uploadFileResponse.setDocumentId("Other Error!");
                return new ResponseEntity<>(uploadFileResponse, HttpStatus.CONFLICT);
            }
        }
        catch (Exception ex) {
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setDocumentId("Other Error!");
            return new ResponseEntity<>(uploadFileResponse, HttpStatus.CONFLICT);
        }

    }

}
