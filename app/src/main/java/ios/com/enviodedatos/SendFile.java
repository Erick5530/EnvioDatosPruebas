package ios.com.enviodedatos;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendFile {
    URL connectURL;
    String responseString;
    String fileName;
    byte[] dataToServer;

    SendFile(String urlString, String fileName ){

        try{
            connectURL = new URL(urlString);
        }catch(Exception ex){

            System.out.println ("URL FORMATION MALFORMATED URL");

        }
        this.fileName = fileName;

    }



    void doStart(FileInputStream stream){

        try {
            fileInputStream = stream;
            thirdTry();
        } catch (IOException ex) {
            //Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    FileInputStream fileInputStream = null;
    void thirdTry() throws IOException {

        String existingFileName = fileName;



        String lineEnd = "\r\n";

        String twoHyphens = "--";

        String boundary = "*****";

        String Tag="3rd";

        try{

            //------------------ CLIENT REQUEST

            System.out.println("Starting to bad things");
            // PHP Service connection

            HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+"*****");
            DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );


            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName +"\"" + lineEnd);




            dos.writeBytes(lineEnd);
            System.out.println("Headers are written");


            int bytesAvailable = fileInputStream.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0){

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Close input stream
            System.out.println("File is written");
            fileInputStream.close();
            dos.flush();

            InputStream is = conn.getInputStream();
            // retrieve the response from server
            int ch;

            StringBuffer b =new StringBuffer();
            while( ( ch = is.read() ) != -1 ){
                b.append( (char)ch );

            }

            String s=b.toString();
            System.out.println("Response"+s);
            dos.close();
        }catch (MalformedURLException ex){

            System.out.println("error: " + ex.getMessage()+  ex);

        }catch (IOException ioe){

            System.out.println( "error: " + ioe.getMessage() + ioe);
        }

    }


}
