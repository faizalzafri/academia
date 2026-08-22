package com.academia.platform.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.academia.platform.model.File;
import com.academia.platform.repository.FileRepository;

@Service
public class FileService {

	private static final Logger logger = LoggerFactory.getLogger(FileService.class);

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private UserService userService;

	public File storeFile(MultipartFile file) {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		if (fileName.contains("..")) {
			throw new IllegalArgumentException("Filename contains invalid path sequence: " + fileName);
		}
		try {
			File dbFile = new File(fileName, file.getContentType(), file.getBytes(), userService.getUser());
			File saved = fileRepository.save(dbFile);
			saved.setUrl("/downloadFile/" + saved.getId());
			return fileRepository.save(saved);
		} catch (IOException e) {
			logger.error("Failed to store file {}: {}", fileName, e.getMessage());
			throw new RuntimeException("Could not store file " + fileName, e);
		}
	}

	public File getFile(int fileId) {
		return fileRepository.findById(fileId)
				.orElseThrow(() -> new IllegalArgumentException("File not found with id: " + fileId));
	}

	public List<File> getAll() {
		return fileRepository.findAll();
	}
}