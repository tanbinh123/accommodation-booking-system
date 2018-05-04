package com.rasto.accommodationbookingsystem.web.ui.views;

import com.rasto.accommodationbookingsystem.HasLogger;
import com.rasto.accommodationbookingsystem.backend.data.AccommodationTypeEnum;
import com.rasto.accommodationbookingsystem.backend.data.entity.Accommodation;
import com.rasto.accommodationbookingsystem.backend.data.entity.AccommodationType;
import com.rasto.accommodationbookingsystem.backend.data.entity.Address;
import com.rasto.accommodationbookingsystem.backend.service.AccommodationService;
import com.rasto.accommodationbookingsystem.backend.service.AccommodationTypesService;
import com.rasto.accommodationbookingsystem.backend.service.AddressService;
import com.rasto.accommodationbookingsystem.security.UserAuthenticationState;
import com.rasto.accommodationbookingsystem.web.ui.MainLayout;
import com.rasto.accommodationbookingsystem.web.ui.components.DropdownItem;
import com.rasto.accommodationbookingsystem.web.ui.utils.PhotoReceiver;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasClickListeners;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.binder.StatusChangeListener;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

@Route(value = "new-accommodation", layout = MainLayout.class)
@Tag("new-accommodation-view")
@HtmlImport("src/views/new-accommodation-view.html")
@PageTitle("New accommodation")
public class NewAccommodationView extends PolymerTemplate<TemplateModel> implements BeforeEnterObserver, HasLogger {

    private final static String FIELD_EMPTY_ERROR_MESSAGE = "Field can not be empty";
    private final static int FIELD_MIN_LENGTH = 1;

    private final AccommodationTypesService accommodationTypesService;
    private final UserAuthenticationState userAuthenticationState;
    private final AccommodationService accommodationService;
    private final AddressService addressService;

    @Id("progressBarDiv")
    private Div progressBarDiv;

    @Id("accommodationName")
    private TextField accommodationName;

    @Id("numGuests")
    private TextField numGuests;

    @Id("numBeds")
    private TextField numBeds;

    @Id("numBaths")
    private TextField numBaths;

    @Id("chosenAccommodationType")
    private TextField chosenAccommodationType;

    @Id("accommodationTypes")
    private VerticalLayout accommodationTypes;

    @Id("country")
    private TextField country;

    @Id("city")
    private TextField city;

    @Id("street")
    private TextField street;

    @Id("pricePerNight")
    private TextField pricePerNight;

    @Id("upload")
    private Upload upload;

    @Id("createButton")
    private Button createButton;

    private final Binder<Accommodation> accommodationBinder = new Binder<>(Accommodation.class);
    private final Binder<AccommodationType> accommodationTypeBinder = new Binder<>(AccommodationType.class);
    private final Binder<Address> addressBinder = new Binder<>(Address.class);
    private AccommodationTypeEnum chosenAccommodationTypeEnum;
    private PhotoReceiver photoReceiver;

    @Autowired
    public NewAccommodationView(AccommodationService accommodationService, AccommodationTypesService accommodationTypesService,
                                AddressService addressService, UserAuthenticationState userAuthenticationState) {
        this.userAuthenticationState = userAuthenticationState;
        this.accommodationTypesService = accommodationTypesService;
        this.accommodationService = accommodationService;
        this.addressService = addressService;
    }

    private void initUploadComponent() {
        photoReceiver = new PhotoReceiver();
        upload.setReceiver(photoReceiver);
        upload.addSucceededListener(event -> {
            if (isFormReady()) {
                createButton.setEnabled(true);
            } else {
                createButton.setEnabled(false);
            }
        });
    }

    private DropdownItem<AccommodationTypeEnum> createDropdownItem(AccommodationType type) {
        DropdownItem<AccommodationTypeEnum> item = new DropdownItem<>();
        item.setClassName("dropdown-item");
        item.setText(type.getName().toString());
        item.setValue(type.getName());
        item.addClickListener((ComponentEventListener<HasClickListeners.ClickEvent<Button>>) event -> {
            chosenAccommodationTypeEnum = ((DropdownItem<AccommodationTypeEnum>) event.getSource()).getValue();
            chosenAccommodationType.setValue(event.getSource().getText());
        });
        return item;
    }

    private void bindForm() {
        accommodationBinder.setBean(accommodationService.createNew());
        accommodationBinder.forField(accommodationName)
                .withValidator(new StringLengthValidator(
                        FIELD_EMPTY_ERROR_MESSAGE,
                        FIELD_MIN_LENGTH, null))
                .bind(accommodation -> accommodationName.getEmptyValue(), Accommodation::setName);

        bindNumField(numGuests, Accommodation::setGuests);
        bindNumField(numBeds, Accommodation::setBeds);
        bindNumField(numBaths, Accommodation::setBathrooms);
        bindNumField(pricePerNight, (accommodation, integer) -> accommodation.setPricePerNight(BigDecimal.valueOf(integer)));

        accommodationTypeBinder.setBean(accommodationTypesService.createNew());
        accommodationTypeBinder.forField(chosenAccommodationType)
                .withValidator(new StringLengthValidator(
                        FIELD_EMPTY_ERROR_MESSAGE,
                        1, null))
                .bind(accommodationType -> chosenAccommodationType.getEmptyValue(), (Setter<AccommodationType, String>) (accommodationType, s) -> {
                    if (s != null && s.equals(chosenAccommodationTypeEnum.toString())) {
                        accommodationType.setName(chosenAccommodationTypeEnum);
                    }
                });

        addressBinder.setBean(addressService.createNew());
        addressBinder.forField(country)
                .withValidator(new StringLengthValidator(
                        FIELD_EMPTY_ERROR_MESSAGE,
                        FIELD_MIN_LENGTH, null))
                .bind(address -> country.getEmptyValue(), Address::setCountry);
        addressBinder.forField(city)
                .withValidator(new StringLengthValidator(
                        FIELD_EMPTY_ERROR_MESSAGE,
                        FIELD_MIN_LENGTH, null))
                .bind(address -> city.getEmptyValue(), Address::setCity);
        addressBinder.forField(street)
                .withValidator(new StringLengthValidator(
                        FIELD_EMPTY_ERROR_MESSAGE,
                        FIELD_MIN_LENGTH, null))
                .bind(address -> street.getEmptyValue(), Address::setStreet);


        StatusChangeListener statusChangeListener = event -> {
            if (isFormReadyWithoutValidation()) {
                createButton.setEnabled(true);
            } else {
                createButton.setEnabled(false);
            }
        };

        accommodationBinder.addStatusChangeListener(statusChangeListener);
        accommodationTypeBinder.addStatusChangeListener(statusChangeListener);
        addressBinder.addStatusChangeListener(statusChangeListener);
    }

    private void bindNumField(TextField numField, BiConsumer<Accommodation, Integer> method) {
        accommodationBinder.forField(numField)
                .withValidator(new StringLengthValidator(
                        FIELD_EMPTY_ERROR_MESSAGE,
                        FIELD_MIN_LENGTH, null))
                .bind(accommodation -> numField.getEmptyValue(), (accommodation, str) -> {
                    if (str != null && !str.isEmpty()) {
                        method.accept(accommodation, Integer.parseInt(str));
                    }
                });
    }

    private void createAccommodation() {
        if (isFormReady()) {
            //showProgressBar();

            Accommodation accommodation = accommodationBinder.getBean();
            AccommodationType type = accommodationTypeBinder.getBean();
            Address address = addressBinder.getBean();

            try {
                accommodationService.saveAccommodationWithPhotos(accommodation, type, address, photoReceiver.getFiles());
                showOnSuccessfullyCreatedAccommodationNotification();
            } catch (IOException e) {
                Notification.show("Uploading photos failed", 3000, Notification.Position.MIDDLE);
                e.printStackTrace();
            } catch (RuntimeException e) {
                Notification.show("Image size too large", 3000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        } else {
            createButton.setEnabled(false);
        }
    }

    private void showProgressBar() {
        createButton.setEnabled(false);
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBarDiv.add(progressBar);
        progressBar.addAttachListener(new ComponentEventListener<AttachEvent>() {
            @Override
            public void onComponentEvent(AttachEvent event) {
                try {
                    accommodationService.saveAccommodationWithPhotos(accommodationBinder.getBean(), accommodationTypeBinder.getBean(), addressBinder.getBean(), photoReceiver.getFiles());
                    showOnSuccessfullyCreatedAccommodationNotification();
                } catch (IOException e) {
                    Notification.show("Uploading photos failed", 3000, Notification.Position.MIDDLE);
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    Notification.show("Image size too large", 3000, Notification.Position.MIDDLE);
                    e.printStackTrace();
                }
            }
        });
    }

    private void showOnSuccessfullyCreatedAccommodationNotification() {
        Label label = new Label("Accommodation successfully added");
        Button button = new Button("OK");
        Notification notification = new Notification(label, button);
        button.addClickListener(e -> {
            notification.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
        notification.setPosition(Notification.Position.MIDDLE);
        notification.open();
    }

    private boolean isFormReadyWithoutValidation() {
        return accommodationBinder.isValid() && accommodationTypeBinder.isValid() && addressBinder.isValid() && !photoReceiver.getFilesPaths().isEmpty();
    }

    private boolean isFormReady() {
        return accommodationBinder.validate().isOk() && accommodationTypeBinder.validate().isOk() && addressBinder.validate().isOk() && !photoReceiver.getFilesPaths().isEmpty();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!userAuthenticationState.isLoggedUserAdmin()) {
            event.rerouteTo(AccommodationsView.class);
            return;
        }

        accommodationName.setAutofocus(true);
        createButton.setEnabled(false);
        createButton.addClickListener(ev -> createAccommodation());

        List<AccommodationType> types = accommodationTypesService.findAll();
        types.forEach(type -> accommodationTypes.add(createDropdownItem(type)));

        bindForm();
        initUploadComponent();
    }
}
