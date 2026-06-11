package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.model.StoreBranch;
import commerce.sbEcommerce.payload.StoreBranchDTO;
import commerce.sbEcommerce.payload.StoreBranchRequest;
import commerce.sbEcommerce.repository.StoreBranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StoreBranchServiceIml implements StoreBranchService {
    @Autowired
    private StoreBranchRepository storeBranchRepository;

    @Override
    public List<StoreBranchDTO> getStores() {
        return storeBranchRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public StoreBranchDTO createStore(StoreBranchRequest request) {
        StoreBranch storeBranch = new StoreBranch();
        applyRequest(storeBranch, request);
        return toDTO(storeBranchRepository.save(storeBranch));
    }

    @Override
    @Transactional
    public StoreBranchDTO updateStore(Long id, StoreBranchRequest request) {
        StoreBranch storeBranch = storeBranchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoreBranch", "id", id));

        applyRequest(storeBranch, request);
        return toDTO(storeBranchRepository.save(storeBranch));
    }

    @Override
    @Transactional
    public void deleteStore(Long id) {
        StoreBranch storeBranch = storeBranchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoreBranch", "id", id));
        storeBranchRepository.delete(storeBranch);
    }

    private void applyRequest(StoreBranch storeBranch, StoreBranchRequest request) {
        storeBranch.setBranchName(request.getBranchName().trim());
        storeBranch.setAddress(request.getAddress().trim());
        storeBranch.setMapUrl(request.getMapUrl().trim());
        storeBranch.setEmbedUrl(request.getEmbedUrl().trim());
        storeBranch.setLatitude(0.0);
        storeBranch.setLongitude(0.0);
    }

    private StoreBranchDTO toDTO(StoreBranch storeBranch) {
        return new StoreBranchDTO(
                storeBranch.getId(),
                storeBranch.getBranchName(),
                storeBranch.getAddress(),
                storeBranch.getMapUrl(),
                storeBranch.getEmbedUrl(),
                storeBranch.getLatitude(),
                storeBranch.getLongitude()
        );
    }
}
