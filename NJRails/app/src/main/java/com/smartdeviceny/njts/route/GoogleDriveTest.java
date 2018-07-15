package com.smartdeviceny.njts.route;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


public class GoogleDriveTest {

    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String APPLICATION_NAME =   "Drive API Java Quickstart";
    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY =      JacksonFactory.getDefaultInstance();
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/drive-java-quickstart");

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
           // System.exit(1);
        }
    }

    public static Credential authorize_java() throws IOException {
        // Load client secrets.
        //InputStream in = Quickstart.class.getResourceAsStream("/client_secret.json");
        InputStream in = new FileInputStream(new File("client_secrets.json")); //Quickstart.class.getResourceAsStream("/client_secret.json");

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                //.setAccessType("offline")
                .build();


        Credential credential =  new GoogleCredential().getApplicationDefault(); // setAccessToken(token); //  new AuthorizationCodeInstalledApp(   flow, new GooglePromptReceiv()).authorize("user");


        System.out.println( "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }
//    public static Credential authorize() throws IOException {
//    {
//        Credential credential =       GoogleAccountCredential.usingOAuth2(this, Collections.singleton(TasksScopes.TASKS));
//        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
//        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
//        // Tasks client
//        service =
//                new com.google.api.services.tasks.Tasks.Builder(httpTransport, jsonFactory, credential)
//                        .setApplicationName("Google-TasksAndroidSample/1.0").build();
//    }
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize_java();
        return new Drive.Builder( HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    public static void main() throws  Exception
    {
        Drive service =getDriveService();
        String pageToken = null;

        Object obj = service.files().get("1yc6JGDvqO9BzVa7oAfjFO53pgiTJr9me").execute();
        System.out.println(obj);

        com.google.api.services.drive.model.File public_dir=null;
        do {
            FileList result = service.files().list().setQ("'public' in parents") // setQ("mimeType='application/vnd.google-apps.folder'").
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (com.google.api.services.drive.model.File file : result.getFiles()) {
                System.out.printf("Found file: %s (%s)\n",
                        file.getName(), file.getId());
                public_dir = file;
            }
        }while( pageToken != null );
        if ( public_dir != null ) {
        // service.files().get(public_dir.getId()).execute();
        }
    }
}
