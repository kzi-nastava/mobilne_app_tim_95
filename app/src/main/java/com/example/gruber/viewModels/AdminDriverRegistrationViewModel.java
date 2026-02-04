package com.example.gruber.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.models.Address;
import com.example.gruber.models.VehicleInfo;
import com.example.gruber.models.enums.UserRole;

public class AdminDriverRegistrationViewModel extends ViewModel {

    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> firstName = new MutableLiveData<>();
    private final MutableLiveData<String> lastName = new MutableLiveData<>();
    private final MutableLiveData<String> phone = new MutableLiveData<>();
    private final MutableLiveData<String> photoUri = new MutableLiveData<>();
    private final MutableLiveData<Address> address = new MutableLiveData<>();
    private final MutableLiveData<VehicleInfo> vehicleInfo = new MutableLiveData<>();

    // GETTERS
    public MutableLiveData<String> getEmail() { return email; }
    public MutableLiveData<String> getPassword() { return password; }
    public MutableLiveData<String> getFirstName() { return firstName; }
    public MutableLiveData<String> getLastName() { return lastName; }
    public MutableLiveData<String> getPhone() { return phone; }
    public MutableLiveData<String> getPhotoUri() { return photoUri; }
    public MutableLiveData<Address> getAddress() { return address; }
    public MutableLiveData<VehicleInfo> getVehicleInfo() { return vehicleInfo; }

    // SETTERS
    public void setEmail(String value) { email.setValue(value); }
    public void setPassword(String value) { password.setValue(value); }
    public void setFirstName(String value) { firstName.setValue(value); }
    public void setLastName(String value) { lastName.setValue(value); }
    public void setPhone(String value) { phone.setValue(value); }
    public void setPhotoUri(String value) { photoUri.setValue(value); }
    public void setAddress(Address value) { address.setValue(value); }
    public void setVehicleInfo(VehicleInfo value) { vehicleInfo.setValue(value); }

    // Reset all data
    public void reset() {
        email.setValue(null);
        password.setValue(null);
        firstName.setValue(null);
        lastName.setValue(null);
        phone.setValue(null);
        photoUri.setValue(null);
        address.setValue(null);
        vehicleInfo.setValue(null);
    }
}
