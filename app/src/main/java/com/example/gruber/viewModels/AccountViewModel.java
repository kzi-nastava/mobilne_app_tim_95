package com.example.gruber.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.models.Address;

public class AccountViewModel extends ViewModel {
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> firstName = new MutableLiveData<>();
    private final MutableLiveData<String> lastName = new MutableLiveData<>();
    private final MutableLiveData<String> phone = new MutableLiveData<>();
    private final MutableLiveData<Address> address = new MutableLiveData<>();
    private final MutableLiveData<String> image = new MutableLiveData<>();

    public void setEmail(String email) {
        this.email.setValue(email);
    }
    public void setPassword(String password) {
        this.password.setValue(password);
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

    public MutableLiveData<String> getPassword() {
        return password;
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
}
