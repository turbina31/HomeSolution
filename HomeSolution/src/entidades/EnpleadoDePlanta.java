package entidades;

import java.util.Map;

public class EnpleadoDePlanta extends Empleado{
	private final double valorDia;
    private String categoria;
    private static final Map<String, Double> tipos_de_CATEGORIA = Map.of(
            "INICIAL", 1.0, "TECNICO", 1.5, "EXPERTO", 2.0);
    private static final double DIAS_LABORABLES_MES = 20.0;
    private static final double ADICIONAL = 1.02; // 2% adicional
    private boolean libre;
    private int proximoLegajo = 10;
    private int legajo = 0;

    public EnpleadoDePlanta(String nombre, double valorDia, String categoria) {
        super(nombre);
        if (categoria == null || categoria.isEmpty())
            throw new RuntimeException("La categoria no puede ser nula o vacía");
        if (!tipos_de_CATEGORIA.containsKey(categoria.toUpperCase()))
            throw new IllegalArgumentException("Categoría inválida: " + categoria);
        this.valorDia = valorDia;
        this.categoria = categoria.toUpperCase();
        this.libre = true;
    }

    @Override
    public boolean estaLibre() {
        return this.libre;
    }

    @Override
    public double calcularCosto(double dias) {
        double factor = tipos_de_CATEGORIA.get(categoria);
        double costoBase = (valorDia * DIAS_LABORABLES_MES * 8.0) / DIAS_LABORABLES_MES * dias * factor;
        return costoBase;
    }
    public double calcularCostoConAdicional(double dias) {
        return calcularCosto(dias) * ADICIONAL;
    }

    @Override
    public boolean esDePlanta() { return true; }
}
