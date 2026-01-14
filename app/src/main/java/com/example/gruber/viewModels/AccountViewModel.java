package com.example.gruber.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.models.Address;
import com.example.gruber.models.User;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.UserService;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AccountViewModel extends ViewModel {

    protected final UserService userService;

    protected final MutableLiveData<String> email = new MutableLiveData<>();
    protected final MutableLiveData<UserRole> role = new MutableLiveData<>();
    protected final MutableLiveData<String> firstName = new MutableLiveData<>();
    protected final MutableLiveData<String> lastName = new MutableLiveData<>();
    protected final MutableLiveData<String> phone = new MutableLiveData<>();
    protected final MutableLiveData<Address> address = new MutableLiveData<>();
    protected final MutableLiveData<String> image = new MutableLiveData<>();

    @Inject
    public AccountViewModel(UserService userService) {
        this.userService = userService;
    }


    public void setEmail(String email) {
        this.email.setValue(email);
    }
    public void setRole(UserRole role) {
        this.role.setValue(role);
    }
    public void setFirstName(String firstName) {
        this.firstName.setValue(firstName);
    }
    public void setLastName(String lastName) {
        this.lastName.setValue(lastName);
    }
    public void setPhone(String phone) {
        this.phone.setValue(phone);
    }
    public void setAddress(Address address) {
        this.address.setValue(address);
    }

    public void setImage(String imageURI) {
        this.image.setValue(imageURI);
    }
    public MutableLiveData<String> getEmail() {
        return email;
    }

    public MutableLiveData<UserRole> getRole() {
        return role;
    }

    public MutableLiveData<String> getFirstName() {
        return firstName;
    }

    public MutableLiveData<String> getLastName() {
        return lastName;
    }

    public MutableLiveData<String> getPhone() {
        return phone;
    }

    public MutableLiveData<Address> getAddress() {
        return address;
    }
    public MutableLiveData<String> getImage() {
        return image;
    }

    // Driver-specific methods (override in DriverViewModel)
    public MutableLiveData<String> getVehicleModel() {
        return new MutableLiveData<>();
    }

    public void setVehicleModel(String vehicleModel) {
        // Override in DriverViewModel
    }

    public MutableLiveData<String> getVehiclePlate() {
        return new MutableLiveData<>();
    }

    public void setVehiclePlate(String vehiclePlate) {
        // Override in DriverViewModel
    }

    public MutableLiveData<Integer> getActiveHours() {
        return new MutableLiveData<>();
    }

    public void setActiveHours(Integer activeHours) {
        // Override in DriverViewModel
    }

    public User toUser() {
        return new User(
                firstName.getValue(),
                lastName.getValue(),
                email.getValue(),
                phone.getValue(),
                role.getValue(),
                image.getValue()
        );
    }

    public void logOut() {
        firstName.setValue("");
        lastName.setValue("");
        email.setValue("");
        phone.setValue("");
        role.setValue(UserRole.GUEST);
        image.setValue("");
    }
}
