package commerce.sbEcommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImp implements FileService {
    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        String originalFileName = file.getOriginalFilename();

        String randomId = UUID.randomUUID().toString();
        String extension = "";
        if (originalFileName != null) {
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFileName.substring(dotIndex);
            }
        }
        String fileName = randomId.concat(extension);

        String filePath = path + File.separator + fileName;


        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdirs();
        }

        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }
}
