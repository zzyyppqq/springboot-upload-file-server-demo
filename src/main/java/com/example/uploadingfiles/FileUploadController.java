package com.example.uploadingfiles;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.util.stream.Collectors;

import com.example.uploadingfiles.model.ResultData;
import com.example.uploadingfiles.model.ReturnCode;
import com.example.uploadingfiles.storage.StorageException;
import com.example.uploadingfiles.util.TimeUtils;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

@Controller
public class FileUploadController {

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	/**
	 * 返回表单【uploadForm.html】页面
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	/**
	 * Html 文件下载
	 * @param filename
	 * @return
	 */
	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	/**
	 * Html 文件上传
	 * @param file
	 * @return
	 */
	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {

		storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}

	/**
	 * 接口上传文件
	 * @param file
	 * @return
	 */
	@PostMapping("/uploadFile")
	@ResponseBody
	public ResultData<String> fileUpload(@RequestParam("file") MultipartFile file) {
		System.out.println("fileUpload: " + file.getName());
		storageService.store(file);
		return ResultData.success("file upload success");
	}

	/**
	 * 接口获取json数据
	 * @return
	 */
	@GetMapping("/json")
	@ResponseBody
	public ResultData<String> getJson() {
		return ResultData.success("json success");
	}

	@GetMapping("/getFileListInfo/{filename}")
	@ResponseBody
	public ResultData<String> getFileInfo(@PathVariable String filename) {
		System.out.println("fileName: " + filename);
		String json = storageService.read(filename);
		return ResultData.success(json);
	}

	@PostMapping("/postFileListInfo/{filename}")
	@ResponseBody
	public ResultData<String> postFileInfo(@RequestBody String json, @PathVariable String filename) {
		System.out.println("fileName: " + filename+ ", fileInfo: " + json);
		storageService.write(json, filename);
		return ResultData.success("postFileInfo success");
	}

	@PostMapping("/postFileListInfo")
	@ResponseBody
	public ResultData<String> postFileInfo(@RequestBody String json) {
		System.out.println("fileListInfo: " + json);
		String time = TimeUtils.formatDateToStr(System.currentTimeMillis(), "yyyy-MM-dd_HH:mm:ss");
		storageService.write(json, String.format("fileListInfo_%s.json", time));
		return ResultData.success("postFileListInfo success");
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	@ExceptionHandler(StorageException.class)
	@ResponseBody
	public ResultData<String> handleStorageFound(StorageException e) {
		return ResultData.fail(ReturnCode.RC500.getCode(),e.getMessage());
	}

}
