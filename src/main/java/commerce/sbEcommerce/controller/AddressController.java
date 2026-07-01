package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.payload.AddressDTO;
import commerce.sbEcommerce.service.AddressService;
import commerce.sbEcommerce.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;

    @Autowired
    AuthUtil authUtil;

    @PostMapping("/auth/user/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO){
        User user = authUtil.getCurrentUserEntity();
        AddressDTO saveAddress = addressService.createAddress(addressDTO, user);
        return new ResponseEntity<>(saveAddress, HttpStatus.CREATED);
    }


    @GetMapping("/admin/user/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses(){
       List<AddressDTO> addressDTOList = addressService.getAdresses();
          return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @GetMapping("/auth/user/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){
        User user = authUtil.getCurrentUserEntity();
        List<AddressDTO> addressDTOList = addressService.getUserAddresses(user);
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @PutMapping("/auth/user/addresses/update/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@Valid @RequestBody AddressDTO addressDTO,
                                                      @PathVariable("addressId") Long addressId) {
        AddressDTO updatedUserAddressDTO = addressService.updateUserAddress(addressDTO, addressId);
        return new ResponseEntity<>(updatedUserAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/auth/user/address/delete/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable("addressId") Long addressId){
        addressService.deleteUserAddress(addressId);
        return new ResponseEntity<>("successfully ", HttpStatus.OK);
    }


}
