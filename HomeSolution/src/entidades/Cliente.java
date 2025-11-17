package entidades;

public class Cliente {
    private String nombre;
    private String telefono;
    private String email;

    public Cliente(String nombre, String telefono, String email) {
        if (nombre == null || nombre.isEmpty())
            throw new IllegalArgumentException("El nombre del cliente no puede ser nulo o vacío");
        if (telefono == null || telefono.isEmpty())
            throw new IllegalArgumentException("El teléfono del cliente no puede ser nulo o vacío");
        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("El email del cliente no puede ser nulo o vacío");
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

	public String obtenerEmail() {
		return email;
	}
    @Override
    public String toString() {
        return "Cliente\nnombre = " + nombre + ",\n telefono = " + telefono + ",\n email = " + email;
    }
}

