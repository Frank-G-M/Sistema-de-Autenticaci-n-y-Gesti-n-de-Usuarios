package com.example.auths3.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface IS3Service {
    // Crear el bucket
    String createBucket(String bucketName);

    //Validar si el bocket existe
    String validatedBucket(String bucketName);

    //Lista de los buckets
    List<String> getAllBucket();

    // Carga de un archivo bucket
    Boolean uploadFile(String bucketName, String key, Path fileLocation);

   // Descarga archivo
    void downloadFile(String bucket, String key) throws IOException;

    //Generar URL prefirmada para subir archivos
    String generetePresignedUploadUrl(String bucketName, String key, Duration duration);

    //Generar URL prefirmada para descargar archivos
    String generetePresignedDownloadUrl(String bucketName, String key, Duration duration);
}