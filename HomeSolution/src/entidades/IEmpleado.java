package entidades;

public interface IEmpleado {
	Integer obtenerLegajo();
    String obtenerNombre();
    double calcularCosto(double dias);
    boolean esDePlanta();
    int obtenerRetrasos();
    void sumarRetraso();
    void liberar();
    boolean estaLibre();
    void marcarAsignado();
    void marcarOcupado();
}
