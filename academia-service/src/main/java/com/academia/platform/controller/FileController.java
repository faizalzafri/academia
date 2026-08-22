package com.academia.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.academia.platform.model.File;
import com.academia.platform.dto.UploadFileResponse;
import com.academia.platform.service.FileService;

@RestController
public class FileController {
	@Autowired
	private FileService fileService;
	
	@PostMapping("/uploadFile")
	public UploadFileResponse uploadFile(@RequestParam(name="file") MultipartFile file) {
		File dbFile = fileService.storeFile(file);

		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(Integer.toString(dbFile.getId())).toUriString();

		return new UploadFileResponse(dbFile.getFileName(), fileDownloadUri, file.getContentType(), file.getSize());
	}
	
	@GetMapping("/downloadFile/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable int fileId) {
		File dbFile = fileService.getFile(fileId);
		
		return ResponseEntity.ok()
							.contentType(MediaType.parseMediaType(dbFile.getFileType()))
							.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
							.body(new ByteArrayResource(dbFile.getData()));
	}

}
