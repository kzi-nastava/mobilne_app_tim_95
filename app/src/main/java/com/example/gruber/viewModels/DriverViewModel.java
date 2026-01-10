package com.example.gruber.viewModels;

import androidx.lifecycle.MutableLiveData;

import com.example.gruber.models.User;
import com.example.gruber.services.UserService;

import javax.inject.Inject;

public class DriverViewModel extends AccountViewModel{
    private final MutableLiveData<Integer> activeHoursLast24 = new MutableLiveData<>();
    private final MutableLiveData<String> vehicleModel = new MutableLiveData<>();
    private final MutableLiveData<String> vehiclePlate = new MutableLiveData<>();

    @Inject
    public DriverViewModel(UserService userService) {
        super(userService);
    }

    public int getActiveHoursLast24() {
        return activeHoursLast24.getValue() != null ? activeHoursLast24.getValue().intValue() : 0;
    }
    public void setActiveHoursLast24(int activeHoursLast24) {
        this.activeHoursLast24.setValue(Integer.valueOf(activeHoursLast24));
    }
    public String getVehicleModel() {
        return vehicleModel.getValue();
    }
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel.setValue(vehicleModel);
    }
    public String getVehiclePlate() {
        return vehiclePlate.getValue();
    }
    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate.setValue(vehiclePlate);
    }

    @Override
    public User toUser() {
        return new User(
                firstName.getValue(),
                lastName.getValue(),
                email.getValue(),
                phone.getValue(),
                role.getValue(),
                image.getValue(),
                vehicleModel.getValue(),
                vehiclePlate.getValue(),
                0
        );
    }
}
