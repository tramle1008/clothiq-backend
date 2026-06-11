package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.payload.StoreBranchDTO;
import commerce.sbEcommerce.payload.StoreBranchRequest;
import commerce.sbEcommerce.service.StoreBranchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StoreBranchController {
    @Autowired
    private StoreBranchService storeBranchService;

    @GetMapping("/stores")
    public ResponseEntity<List<StoreBranchDTO>> getStores() {
        return new ResponseEntity<>(storeBranchService.getStores(), HttpStatus.OK);
    }

//    @PostMapping("/stores/nearest")
//    public ResponseEntity<NearestStoreResponse> findNearestStore(@Valid @RequestBody NearestStoreRequest request) {
//        return new ResponseEntity<>(storeBranchService.findNearestStore(request), HttpStatus.OK);
//    }

    @PostMapping("/admin/stores")
    public ResponseEntity<StoreBranchDTO> createStore(@Valid @RequestBody StoreBranchRequest request) {
        return new ResponseEntity<>(storeBranchService.createStore(request), HttpStatus.CREATED);
    }

    @PutMapping("/admin/stores/{id}")
    public ResponseEntity<StoreBranchDTO> updateStore(@PathVariable Long id,
                                                      @Valid @RequestBody StoreBranchRequest request) {
        return new ResponseEntity<>(storeBranchService.updateStore(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/admin/stores/{id}")
    public ResponseEntity<String> deleteStore(@PathVariable Long id) {
        storeBranchService.deleteStore(id);
        return new ResponseEntity<>("Store deleted successfully", HttpStatus.OK);
    }
}
