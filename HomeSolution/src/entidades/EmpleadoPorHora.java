package entidades;

public class EmpleadoPorHora extends Empleado{
	private final double valorHora;

    public EmpleadoPorHora(String nombre, double valorHora) {
        super(nombre);
        this.valorHora = valorHora;
    }

    @Override
    public double calcularCosto(double dias) {
        return valorHora * 8 * Math.ceil(dias); // medio día = día completo
    }
}
