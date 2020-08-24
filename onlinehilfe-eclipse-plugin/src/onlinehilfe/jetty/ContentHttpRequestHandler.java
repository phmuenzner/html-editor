package onlinehilfe.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

import onlinehilfe.CurrentPropertiesStore;
import onlinehilfe.contentbuilder.FilesUtil;

public class ContentHttpRequestHandler extends AbstractHandler {

	private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(
			System.getProperty("java.io.tmpdir"));

	private static final String TARGET_CONTENT_GET_PRE = "/content/get/";
	// private static final String TARGET_CONTENT_POST_PRE = "/content/post/";
	private static final String TARGET_CONTENT_IMAGES = "/_images/";
	private static final String TARGET_CONTENT_FILE_UPLOAD = "/static/jodit/htmleditor/file/upload";
	private static final String TARGET_CONTENT_FILE_BROWSE = "/static/jodit/htmleditor/file/browse";

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println(String.format(
				"ContentHttpRequestHandler handle :: Target: %s, BaseRequest: %s, Request: %s, Response: %s", target,
				baseRequest, request, response));

		

		if (target.startsWith(TARGET_CONTENT_GET_PRE)) {
			// content zum editieren lesen
			response.setContentType("text/html; charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);

			String filePath = target.substring(TARGET_CONTENT_GET_PRE.length());
			System.out.println(TARGET_CONTENT_GET_PRE + ": " + filePath);

			File file = new File(filePath);

			char[] buffer = new char[1024];
			int size = -1;

			try (Reader reader = new InputStreamReader(new FileInputStream(file), FilesUtil.CHARSET);
					PrintWriter writer = response.getWriter()) {
				while ((size = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, size);
				}
			}

			baseRequest.setHandled(true);
			/*
			 * } else if (target.startsWith(TARGET_CONTENT_POST_PRE)) { //
			 * content aus editor speichern
			 * 
			 * response.setStatus(HttpServletResponse.SC_OK);
			 * 
			 * String filePath =
			 * target.substring(TARGET_CONTENT_POST_PRE.length());
			 * System.out.println(TARGET_CONTENT_POST_PRE + ": " + filePath);
			 * 
			 * File file = new File(filePath);
			 * 
			 * char[] buffer = new char[1024]; int size = -1; try (Reader reader
			 * = request.getReader(); Writer writer = new FileWriter(file)) {
			 * while ((size = reader.read(buffer)) != -1) { writer.write(buffer,
			 * 0, size); } }
			 * 
			 * baseRequest.setHandled(true);
			 */
		} else if (target.startsWith(TARGET_CONTENT_FILE_UPLOAD)) {
			// upload
			response.setContentType("text/html; charset=utf-8");	
			// request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
			request.setAttribute("org.eclipse.jetty.multipartConfig", MULTI_PART_CONFIG);
			

			Part filePart = request.getParts().stream().filter(f -> f.getName().startsWith("files")).findFirst().get();
			IPath path = CurrentPropertiesStore.getInstance().getProject().getLocation().append(TARGET_CONTENT_IMAGES);

			int lastDotPos = filePart.getSubmittedFileName().lastIndexOf('.');
			File file = null;
			for (file = path.append(filePart.getSubmittedFileName()).toFile(); file.exists(); file = path
					.append(filePart.getSubmittedFileName().substring(0, lastDotPos) + "-" + System.currentTimeMillis()
							+ filePart.getSubmittedFileName().substring(lastDotPos))
					.toFile()) {
			}

			byte[] buffer = new byte[2048];
			int size = -1;
			try (InputStream in = filePart.getInputStream(); OutputStream out = new FileOutputStream(file)) {
				while ((size = in.read(buffer)) != -1) {
					out.write(buffer, 0, size);
				}
			}

			try (Writer writer = response.getWriter()) {
				writer.write("{ \"msg\": \"File was uploaded\", \"error\": 0, \"images\": [\"" + TARGET_CONTENT_IMAGES
						+ file.getName() + "\"] }");
			}
			response.setContentType("text/json; charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);

		} else if (target.startsWith(TARGET_CONTENT_FILE_BROWSE)) {
			response.setContentType("text/json; charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			
			String requestAction = request.getParameter("action");
			if (requestAction.startsWith("files")) {
				IPath imagePath = CurrentPropertiesStore.getInstance().getProject().getFullPath().append(TARGET_CONTENT_IMAGES);
				IFolder imageFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(imagePath);
				try {
					System.out.println("Members"+imageFolder.members());
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if (imageFolder.exists()) {
					try (Writer writer = response.getWriter()) {
						String fileListStr = "\"files\": [";
						List<String> filesList = new ArrayList<String>();
						for (IResource member: imageFolder.members()) {
							if (member instanceof IFile) {
								IFile file = (IFile) member;
								
								long size = file.getFullPath().toFile().length();
								String sizeString = " byte"; 
								if (size > 1024) {
									size = size/1024;
									sizeString = " kB";
								}
								if (size > 1024) {
									size = size/1024;
									sizeString = " MB";
								}
								sizeString = size + sizeString;
								
								filesList.add("{ \"file\": \"" + file.getName() + "\", "
										+ "\"changed\": \"" + formatTimestamp(file.getLocalTimeStamp()) + "\", "
										+ "\"isImage\": true, "
										+ "\"size\": \"" + sizeString + "\","
										+ "\"thumb\": \"" + file.getName() +"\"}");
							}						
						}
						fileListStr += String.join(",", filesList);
						fileListStr += "]}";
						
						writer.write("{\"success\":true, \"time\":\"2020-05-05 09:30:27\","
								+ "\"data\": {\"code\":220, \"sources\": {\"default\":{"
								+ "\"baseurl\":\"http://"+request.getServerName()+":"+request.getServerPort()+ TARGET_CONTENT_IMAGES+"\","
								+ "\"path\": \"\","
								+ fileListStr+"}}}");
						
					} catch (CoreException e) {
						e.printStackTrace();
					};
				}
			} else if (requestAction.startsWith("folders")) {
				try (Writer writer = response.getWriter()) {
					writer.write("{\"success\":true, \"time\":\"2020-05-05 09:30:27\","
									+ "\"data\": {\"code\":220, \"sources\": {\"default\":{"
									+ "\"baseurl\":\"http://"+request.getServerName()+":"+request.getServerPort()+ TARGET_CONTENT_IMAGES+"\","
									+ "\"path\": \"\","
									+ "\"folders\": [\".\"]}}}}");
				}
			} else if (requestAction.startsWith("permissions")) {
				try (Writer writer = response.getWriter()) {
					writer.write("{\"success\":true, \"time\":\"2020-05-05 09:30:27\","
									+ "\"data\": {\"code\":220, \"sources\": {\"default\":{"
									+ "\"baseurl\":\"http://"+request.getServerName()+":"+request.getServerPort()+ TARGET_CONTENT_IMAGES+"\","
									+ "\"path\": \"\","
									+ "\"permissions\": {"
									+ "\"allowFileMove\": false,"
									+ "\"allowFileRemove\": false,"
									+ "\"allowFileRename\": false,"
									+ "\"allowFileUpload\": true,"
									+ "\"allowFileUploadRemote\": false,"
									+ "\"allowFiles\": true,"
									+ "\"allowFolderCreate\": false,"
									+ "\"allowFolderMove\": false,"
									+ "\"allowFolderRemove\": false,"
									+ "\"allowFolderRename\": false,"
									+ "\"allowFolders\": true,"
									+ "\"allowImageCrop\": false,"
									+ "\"allowImageResize\": false"
									+ "}}}}}");
				}
			}
			baseRequest.setHandled(true);
			
		} else {
			response.setContentType("text/html; charset=utf-8");
			// links
			IPath path = CurrentPropertiesStore.getInstance().getProject().getLocation().append(target);
			File file = path.toFile();

			if (!file.exists()) {
				System.out.println(" === Nicht gefundene Datei: " + file);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}

			String mimeType = Files.probeContentType(file.toPath());
			if ("text/html".equals(mimeType)) {
				response.setContentType("text/html; charset=utf-8");
			} else {
				response.setContentType(mimeType);
			}
			byte[] buffer = new byte[2048];
			int size = -1;
			try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
				while ((size = in.read(buffer)) != -1) {
					out.write(buffer, 0, size);
				}
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
			}
		}

	}

	private static final DateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
	
	private static String formatTimestamp(long timestamp) {
		return dateformat.format(new Date(timestamp));
	} 
}
