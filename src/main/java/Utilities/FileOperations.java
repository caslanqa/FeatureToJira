package Utilities;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.restassured.RestAssured.given;


public class FileOperations {

    public static String repoFolder;

    public static String authToken;

    private static final String XRAY_URI = "https://xray.cloud.getxray.app/api/v2";

    public static List<String> getFileAsList(String filePath){
        List<String> filewrapper = new ArrayList<>();
        try {
            FileReader fr = new FileReader(filePath, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(fr);
            String line;

            while((line= br.readLine()) != null){
                line = line.replaceAll("#","");
                if (!line.isEmpty())
                    filewrapper.add(line.trim());
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return filewrapper;
    }


    public static void multiplyFeatureFiles(List<String> filesPaths) {
        for (int i = 0; i < filesPaths.size() ; i++) {
            List<String> fileAsList = FileOperations.getFileAsList(filesPaths.get(i));
            try {
                multiplyFeatureFile(fileAsList,(i+1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void multiplyFeatureFile(List<String> fileAsList, int i) {
        List<List<String>> multipliedScenarios = InventoryOperator.multiplyWithExamples(fileAsList);
        String featureLine = multipliedScenarios.remove(0).get(0);
        writeToFeature(featureLine,multipliedScenarios, i);
    }

    private static void writeToFeature(String featureLine, List<List<String>> multipliedScenarios, int fileIndx) {
        String directoryPath = System.getProperty("user.home").concat("/Downloads/Scenarios/Feature").concat(String.valueOf(fileIndx)+"/");

        for (int i = 0; i < multipliedScenarios.size(); i++) {
            String filePath = directoryPath.concat("scenario").concat(String.valueOf(i+1)).concat(".feature");

            try {
                Path directory = Paths.get(directoryPath);
                if (!Files.exists(directory)) {
                    Files.createDirectories(directory);
                }

                FileWriter fw = new FileWriter(filePath, StandardCharsets.UTF_8);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(featureLine);
                bw.newLine();
                bw.write(multipliedScenarios.get(i).get(0));
                bw.newLine();

                for (int j = 1; j < multipliedScenarios.get(i).size(); j++) {
                    bw.write(multipliedScenarios.get(i).get(j));
                    bw.newLine();
                }
                bw.close();
                fw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    public static void compressFeatures(int fileIndx) {
        String directoryPath = System.getProperty("user.home").concat("/Downloads/Scenarios/Feature").concat(String.valueOf(fileIndx));
        String zipFileName = System.getProperty("user.home").concat("/Downloads/Scenarios/Feature").concat(String.valueOf(fileIndx)+".zip");

        try {
            zipFolder(directoryPath, zipFileName);
            deleteFolder(directoryPath);
        } catch (IOException e) {
            System.err.println("Error compressing folder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void zipFolder(String sourceFolder, String zipFileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFileName);
        ZipOutputStream zos = new ZipOutputStream(fos);

        addFolderToZip(sourceFolder, sourceFolder, zos);

        zos.close();
        fos.close();
    }

    private static void addFolderToZip(String folderPath, String sourceFolder, ZipOutputStream zos) throws IOException {
        File folder = new File(folderPath);
        for (String fileName : folder.list()) {
            if (folderPath.equals(sourceFolder)) {
                addFileToZip(folderPath + File.separator + fileName, fileName, zos);
            } else {
                addFileToZip(folderPath + File.separator + fileName, sourceFolder + File.separator + fileName, zos);
            }
        }
    }

    private static void addFileToZip(String filePath, String sourceFilePath, ZipOutputStream zos) throws IOException {
        File file = new File(filePath);
        if (file.isDirectory()) {
            addFolderToZip(filePath, sourceFilePath, zos);
        } else {
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(filePath);
            zos.putNextEntry(new ZipEntry(sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separator) + 1)));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    private static void deleteFolder(String folderPath) throws IOException {
        Path directory = Paths.get(folderPath);
        Files.walk(directory)
                .sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static String getAuthToken() {
        // Set the base URI for the auth endpoint
        RestAssured.baseURI = XRAY_URI; // Replace with your auth API endpoint

        // Create request specification for the auth request
        RequestSpecification authRequest = given()
                .header("Content-Type", "application/json")
                .body("{\"client_id\": \"3018398D191E476781E9C46AF71E7849\",\"client_secret\": \"9365535bb0675aec7a7ab2da90bc7b38ac7b2f16e2464f44a61be3ec54a67260\"}");

        Response authResponse = authRequest.post("/authenticate"); // Replace with your auth API path

        // Check if the request was successful and return the token
        if (authResponse.getStatusCode() == 200) {
            return authResponse.asString().substring(1,authResponse.asString().length()-1);
        } else {
            System.out.println("Failed to get auth token. Status Code: " + authResponse.getStatusCode());
            return null;
        }
    }

    public static Boolean uploadZipFile(int indx) {
        String zipFilePath = System.getProperty("user.home").concat("/Downloads/Scenarios/Feature").concat(String.valueOf(indx)+".zip");
        // Set the base URI for the upload endpoint
        RestAssured.baseURI = XRAY_URI; // Base URI of your upload API endpoint

        // Path to the ZIP file
        File zipFile = new File(zipFilePath);

        // Create request specification for the upload request
        RequestSpecification uploadRequest = given()
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "multipart/form-data")
                .queryParam("projectKey", "SCRUM")
                .multiPart("file", zipFile);

        Response uploadResponse = null;
        try {
            uploadResponse = uploadRequest.post("/import/feature");

            System.out.println(uploadResponse.getStatusCode());
        } catch (Exception ignored) {
        }
        return uploadResponse.getStatusCode()==200?true:false;
    }
}


















