package com.stream.app.controllers;

import com.stream.app.AppConstants;
import com.stream.app.entities.Video;
import com.stream.app.playload.CustomMessage;
import com.stream.app.services.VideoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {


    private VideoService videoService;

    public VideoController(
            VideoService videoService) {
        this.videoService = videoService;
    }


    @PostMapping
    public ResponseEntity<?> create(@RequestParam("file") MultipartFile file, @RequestParam("title") String title, @RequestParam("description") String description) {

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());

        Video savedVideo = videoService.save(video, file);

        if (savedVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CustomMessage.builder().message("Video not uploaded ").success(false).build());
        }


    }


    //get all videos
    @GetMapping
    public List<Video> getAll() {
        return videoService.getAll();
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable String videoId) {

        Video video = videoService.get(videoId);

        String contentType = video.getContentType();

        String filePath = video.getFilePath();

        Resource resource = new FileSystemResource(filePath);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }


        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);


    }


    // stream video in chunks
    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(@PathVariable String videoId, @RequestHeader(value = "Range", required = false) String range) {

        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());

        Resource resource = new FileSystemResource(path);

        String contentType = video.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";

        }

        //file ki length
        long fileLength = path.toFile().length();


        if (range == null) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        }

        //calculating start and end range

        long rangeStart;

        long rangeEnd;

        String[] ranges = range.replace("bytes=", "").split("-");
        rangeStart = Long.parseLong(ranges[0]);

        rangeEnd = rangeStart + AppConstants.CHUNK_SIZE - 1;

        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1;
        }

        InputStream inputStream;

        try {

            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;


            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));


        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Value("${file.video.hsl}")
    private String HSL_DIR;


    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId) {
        Path path = Paths.get(HSL_DIR, videoId, "master.m3u8");
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/x-mpegURL").body(resource);

    }

    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegment(@PathVariable String videoId,
                                                 @PathVariable String segment) {
        Path path = Paths.get(HSL_DIR, videoId, segment + ".ts");
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "video/mp2t").body(resource);

    }

}
