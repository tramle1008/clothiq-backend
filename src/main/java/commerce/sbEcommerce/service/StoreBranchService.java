package commerce.sbEcommerce.service;

import commerce.sbEcommerce.payload.StoreBranchDTO;
import commerce.sbEcommerce.payload.StoreBranchRequest;

import java.util.List;

public interface StoreBranchService {
    List<StoreBranchDTO> getStores();
    StoreBranchDTO createStore(StoreBranchRequest request);
    StoreBranchDTO updateStore(Long id, StoreBranchRequest request);
    void deleteStore(Long id);
}
