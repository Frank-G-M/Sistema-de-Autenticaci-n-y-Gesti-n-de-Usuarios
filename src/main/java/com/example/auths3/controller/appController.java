package com.example.auths3.controller;


import com.example.auths3.service.IS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("s3")
public class appController {

    @Value("${spring.destination.folder}")
    private String destinationFolder;
    @Autowired
    private IS3Service service;
    @PostMapping("/create")
    public ResponseEntity<String> createBucket(@RequestParam String bucketName){
        return ResponseEntity.ok(this.service.createBucket(bucketName));
    }
    @GetMapping("/check/{bucketName}")
    public ResponseEntity<String> checkBucket(@PathVariable String bucketName){
        return ResponseEntity.ok(this.service.validatedBucket(bucketName));
    }
    @GetMapping("/list")
    public ResponseEntity<List<String>> listBuckets(){
        return ResponseEntity.ok(this.service.getAllBucket());
    }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam String bucketName, @RequestParam String key,@RequestPart MultipartFile file)throws IOException{
        try{
            Path staticDir = Paths.get(destinationFolder);
            if(!Files.exists(staticDir)){
                Files.createDirectories(staticDir);
            }
            Path filePath = staticDir.resolve(file.getOriginalFilename());
            Path finalPath = Files.write(filePath, file.getBytes());
            Boolean result = this.service.uploadFile(bucketName,key, finalPath);
            if(result){
                Files.delete(finalPath);
                return ResponseEntity.ok("Cargado correctamente");
            }else {
                return ResponseEntity.internalServerError().body("Error en el cargue");
            }
        }catch (IOException exception){
            throw new IOException("Error en procesamiento");
        }
    }

    @PostMapping("/download")
    public ResponseEntity<String> downloadFile(@RequestParam String bucketName,@RequestParam String key) throws IOException {
        this.service.downloadFile(bucketName, key);
        return ResponseEntity.ok("Archivo descargado");
    }

    @PostMapping("/upload/presigned")
    public ResponseEntity<String>generatedPresingnedUploadURL(@RequestParam String bucketName, @RequestParam String key, @RequestParam Long time){
        Duration durationToLive = Duration.ofMinutes(time);
        return ResponseEntity.ok( this.service.generetePresignedUploadUrl(bucketName,key,durationToLive));
    }

    @PostMapping("download/presigned")
    public ResponseEntity<String>generatedPresingnedDownloadURL(@RequestParam String bucketName, @RequestParam String key, @RequestParam Long time) {
        Duration durationToLive = Duration.ofMinutes(time);
        return ResponseEntity.ok(this.service.generetePresignedDownloadUrl(bucketName, key, durationToLive));
    }
}