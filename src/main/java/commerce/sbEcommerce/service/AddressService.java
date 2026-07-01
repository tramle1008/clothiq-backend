package commerce.sbEcommerce.service;

import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.payload.AddressDTO;
import commerce.sbEcommerce.payload.ProvinceDTO;
import commerce.sbEcommerce.payload.WardDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);

    List<AddressDTO> getAdresses();

    List<AddressDTO> getUserAddresses(User user);

    AddressDTO updateUserAddress(@Valid AddressDTO addressDTO, Long addressId);

    AddressDTO deleteUserAddress(Long addressId);

    List<ProvinceDTO> getProvinces();

    List<WardDTO> getWardsByProvince(Long provinceId);
}
