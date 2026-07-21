package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.APIException;
import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.exceptioons.UnauthorizedException;
import commerce.sbEcommerce.model.Address;
import commerce.sbEcommerce.model.Province;
import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.model.Ward;
import commerce.sbEcommerce.payload.AddressDTO;
import commerce.sbEcommerce.payload.ProvinceDTO;
import commerce.sbEcommerce.payload.WardDTO;
import commerce.sbEcommerce.repository.AddressRepository;
import commerce.sbEcommerce.repository.ProvinceRepository;
import commerce.sbEcommerce.repository.WardRepository;
import commerce.sbEcommerce.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressIml implements AddressService {
    @Autowired
    AddressRepository addressRepository;

    @Autowired
    WardRepository wardRepository;

    @Autowired
    ProvinceRepository provinceRepository;

    @Autowired
    AuthUtil authUtil;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Ward ward = getWard(addressDTO.getWardId());

        Address address = new Address();
        address.setWard(ward);
        address.setDetail(addressDTO.getDetail());
        address.setPhoneNumber(addressDTO.getPhoneNumber());
        address.setUser(user);

        List<Address> addressList = user.getAddressList();
        addressList.add(address);
        user.setAddressList(addressList);

        Address saveAddress = addressRepository.save(address);
        return mapToDTO(saveAddress);
    }

    @Override
    public List<AddressDTO> getAdresses() {
        List<Address> addressList = addressRepository.findAll();
        return addressList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addressList = addressRepository.findByUser(user);
        return addressList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

    }

    @Override
    public AddressDTO updateUserAddress(AddressDTO addressDTO, Long addressId) {
        User currentUser = authUtil.getCurrentUserEntity();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("address", "addressId", addressId));

        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("You are not allowed to update this address.");
        }

        if (addressDTO.getWardId() != null) {
            address.setWard(getWard(addressDTO.getWardId()));
        }

        if (addressDTO.getDetail() != null) {
            address.setDetail(addressDTO.getDetail());
        }

        if (addressDTO.getPhoneNumber() != null) {
            address.setPhoneNumber(addressDTO.getPhoneNumber());
        }

        Address saveAddress = addressRepository.save(address);
        return mapToDTO(saveAddress);
    }

    @Override
    public AddressDTO deleteUserAddress(Long addressId) {
        User currentUser = authUtil.getCurrentUserEntity();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("address", "addressId", addressId));

        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("You are not allowed to delete this address.");
        }

        addressRepository.delete(address);
        return mapToDTO(address);
    }

    @Override
    public List<ProvinceDTO> getProvinces() {
        return provinceRepository.findAll().stream()
                .map(this::mapProvinceToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WardDTO> getWardsByProvince(Long provinceId) {
        provinceRepository.findById(provinceId)
                .orElseThrow(() -> new ResourceNotFoundException("province", "provinceId", provinceId));

        return wardRepository.findByProvinceProvinceId(provinceId).stream()
                .map(this::mapWardToDTO)
                .collect(Collectors.toList());
    }

    private Ward getWard(Long wardId) {
        if (wardId == null) {
            throw new APIException("wardId is required");
        }

        return wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("ward", "wardId", wardId));
    }

    private AddressDTO mapToDTO(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(address.getAddressId());
        addressDTO.setDetail(address.getDetail());
        addressDTO.setPhoneNumber(address.getPhoneNumber());

        Ward ward = address.getWard();
        if (ward != null) {
            addressDTO.setWardId(ward.getWardId());
            addressDTO.setWardName(ward.getName());

            if (ward.getProvince() != null) {
                addressDTO.setProvinceId(ward.getProvince().getProvinceId());
                addressDTO.setProvinceName(ward.getProvince().getName());
            }
        }

        return addressDTO;
    }

    private ProvinceDTO mapProvinceToDTO(Province province) {
        ProvinceDTO provinceDTO = new ProvinceDTO();
        provinceDTO.setProvinceId(province.getProvinceId());
        provinceDTO.setName(province.getName());
        return provinceDTO;
    }

    private WardDTO mapWardToDTO(Ward ward) {
        WardDTO wardDTO = new WardDTO();
        wardDTO.setWardId(ward.getWardId());
        wardDTO.setName(ward.getName());

        if (ward.getProvince() != null) {
            wardDTO.setProvinceId(ward.getProvince().getProvinceId());
        }

        return wardDTO;
    }
}
