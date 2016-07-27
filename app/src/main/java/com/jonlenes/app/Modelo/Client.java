package com.jonlenes.app.Modelo;


/**
 * Created by Jonlenes on 16/07/2016.
 */
public class Client {
    private Long id;
    private String name;
    private String telephone;
    private byte[] image;

    public Client(Long id, String name, String telephone, byte[] image) {
        this.id = id;
        this.name = name;
        this.telephone = telephone;
        this.image = image;
    }

    public Client(String name, String telephone, byte[] image) {
        this.name = name;
        this.telephone = telephone;
        this.image = image;
    }

    public Client(Long id, String name, String telephone) {
        this.id = id;
        this.name = name;
        this.telephone = telephone;
    }

    public Client(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Client(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return name;
    }
}
