package com.rasto.accommodationbookingsystem.web.ui.components;

import com.rasto.accommodationbookingsystem.HasLogger;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasClickListeners;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Tag("booking-form")
@HtmlImport("src/components/booking-form.html")
public class BookingForm extends PolymerTemplate<BookingForm.Model> implements HasLogger {

    public final String CURRENCY_DEFAULT = "CZK";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private String currency;

    @Id("priceSpan")
    private Span priceSpan;

    @Id("book")
    private Button bookButton;

    @Id("totalTextSpan")
    private Span totalTextSpan;

    @Id("totalPriceSpan")
    private Span totalPriceSpan;

    public interface Model extends TemplateModel {
        String getDateFrom();
        String getDateTo();
    }

    public BookingForm() {
        /*JsonFactory jsonFactory = new JreJsonFactory();
        jsonFactory.createArray();
        jsonFactory.parse("[1524787200]");
        JsonValue jsonValue = new JreJsonArray(jsonFactory);
        getElement().setPropertyJson("disabledDays", jsonValue);*/ //TODO Disabled days
        bookButton.setEnabled(false);
        setTotalVisible(false);
        currency = CURRENCY_DEFAULT;
    }

    public void setTotalVisible(boolean visible) {
        totalPriceSpan.setVisible(visible);
        totalTextSpan.setVisible(visible);
    }

    public void setOnBookButtonClickListener(OnBookButtonClickListener onBookButtonClickListener) {
        bookButton.addClickListener((ComponentEventListener<HasClickListeners.ClickEvent<Button>>) event -> {
            onBookButtonClickListener.onClick(getCheckInDate(), getCheckOutDate());
        });
    }

    public void setOnCheckOutDateChangeListener(OnCheckOutDateChangeListener onCheckOutDateChangeListener) {
        getElement().addPropertyChangeListener("dateTo", event -> {
            if (!event.getValue().toString().isEmpty()) {
                getLogger().info(getCheckInDate().toString() + " " + getCheckOutDate().toString());
                setTotalVisible(true);
                bookButton.setEnabled(true);
            } else {
                setTotalVisible(false);
                bookButton.setEnabled(false);
            }
        });
    }

    public void setTotalPriceSpanText(String text) {
        totalPriceSpan.setText(text);
    }

    public void setTotalPrice(BigDecimal price) {
        totalPriceSpan.setText(price.toString() + " " + currency);
    }

    public void setPriceSpanText(String text) {
        priceSpan.setText(text);
    }

    public void setPrice(BigDecimal price) {
        priceSpan.setText(price.toString() + " " + currency);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getCheckInDate() {
        return LocalDate.parse(getModel().getDateFrom(), formatter);
    }

    public LocalDate getCheckOutDate() {
        return LocalDate.parse(getModel().getDateTo(), formatter);
    }

    public interface OnBookButtonClickListener {
        void onClick(LocalDate checkIn, LocalDate checkOut);
    }

    public interface OnCheckOutDateChangeListener {
        void onChange(LocalDate checkIn, LocalDate checkOut);
    }
}