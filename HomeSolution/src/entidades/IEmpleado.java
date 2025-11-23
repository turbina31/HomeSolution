package entidades;

public interface IEmpleado {
	Integer obtenerLegajo();
    String obtenerNombre();
    double calcularCosto(double dias);
    int obtenerRetrasos();
    public boolean estaAsignado();
    void sumarRetraso();
    void marcarAsignado(boolean asignado);
}
