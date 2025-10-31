package entidades;

public abstract class Trabajador {
    protected String nombre;
    protected double valorHora;

    public Trabajador(String nombre, double valorHora) {
        if (nombre == null || nombre.isEmpty())
            throw new RuntimeException("El nombre del empleado no puede ser nulo o vacío");
        if (valorHora < 0)
            throw new RuntimeException("El valor hora debe ser mayor o igual a 0");
        this.nombre = nombre;
        this.valorHora = valorHora;
    }

    public abstract boolean estaLibre();
    public abstract void marcarLibre();
    public abstract void marcarAsignado();
    public abstract double calcularCosto(double dias);
}
