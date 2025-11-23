package entidades;

import java.time.LocalDate;
import java.util.*;

public class HomeSolution implements IHomeSolution{
	private HashMap<Integer, IEmpleado> empleados;
    private Stack<IEmpleado> empleadosLibres;
    private HashMap<String, Tarea> tareas;
    private HashMap<Integer, Proyecto> proyectos;

    public HomeSolution() {
        this.empleados = new HashMap<>();
        this.empleadosLibres = new Stack<>();
        this.tareas = new HashMap<>();
        this.proyectos = new HashMap<>();
    }

	@Override
	public void registrarEmpleado(String nombre, double valor) throws IllegalArgumentException {
        try {
            if (empleados.containsKey(nombre))
                throw new IllegalArgumentException("El nombre: " + nombre + " ya está registrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if(valor < 0)
            throw new IllegalArgumentException("El valor no puede ser menor a 0: " + valor);
        EmpleadoPorHora empleado = new EmpleadoPorHora(nombre, valor);
        empleados.put(empleado.obtenerLegajo(), empleado);
        agregarEmpleadoLibre(empleado);
	}

	@Override
	public void registrarEmpleado(String nombre, double valor, String categoria) throws IllegalArgumentException {
		if (empleados.containsKey(nombre))
            throw new IllegalArgumentException("El nombre: " + nombre + " ya está registrado");
        EnpleadoDePlanta empleado = new EnpleadoDePlanta(nombre, valor, categoria);
        empleados.put(empleado.obtenerLegajo(), empleado);
        agregarEmpleadoLibre(empleado);
	}

	@Override
	public void registrarProyecto(String[] titulos, String[] descripcion, double[] dias, String domicilio,
			String[] cliente, String inicio, String fin) throws IllegalArgumentException {
		try {
            for (String titulo : titulos) {
                if (tareas.containsKey(titulo)) {
                    throw new IllegalArgumentException("El titulo: " + titulo + " ya esta registrado.");
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Advertencia: " + e.getMessage());
        }
        String nombre = cliente[0];
        String telefono = cliente[1];
        String email = cliente[2];
        Cliente c = new Cliente(nombre, telefono, email);
        //clientes.put(email, c);

        int codProyecto = proyectos.size() + 1;

        List<Tarea> tareasProyecto = new ArrayList<>();
        for (int i = 0; i < titulos.length; i++) {
            Tarea tarea = new Tarea(titulos[i], descripcion[i], dias[i]);
            tareasProyecto.add(tarea);
            tareas.put(titulos[i], tarea);
        }

        Proyecto proyecto = new Proyecto(codProyecto, domicilio, inicio, fin);
        proyecto.agregarCliente(c);
        //for (Tarea tarea : tareasProyecto)
          //  proyecto.agregarTarea(titulos, descripcion, dias);
        for (int i = 0; i < tareasProyecto.toArray().length; i++) {
            proyecto.agregarTarea(titulos[i], descripcion[i], dias[i]);
        }
        proyectos.put(codProyecto, proyecto);
	}

	@Override
	public void asignarResponsableEnTarea(Integer numero, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");

        Tarea tarea = proyecto.obtenerTarea(titulo);
        if (tarea == null)
            throw new RuntimeException("La tarea no está registrada");

        IEmpleado empleadoAnterior = tarea.obtenerEmpleado();
        if (empleadoAnterior != null && !empleadoAnterior.estaLibre()) {
            empleadoAnterior.estaLibre();
            agregarEmpleadoLibre(empleadoAnterior);
        }

		if(empleadosLibres.isEmpty()){
            throw new Exception("No hay empleados disponibles");
        }
		
        IEmpleado empleadoAAsignar = empleadosLibres.pop();
    
        empleadoAAsignar.marcarAsignado(true);
        tarea.asignarEmpleado(empleadoAAsignar);
	}

	@Override
	public void asignarResponsableMenosRetraso(Integer numero, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");
        if (!tareas.containsKey(titulo))
            throw new RuntimeException("La tarea no está registrada");

        Tarea tarea = proyecto.obtenerTarea(titulo);
        if (tarea.obtenerEstado().equals("FINALIZADO"))
            throw new RuntimeException("No se pueden asignar empleados a una tarea finalizada");

        IEmpleado mejorCandidato = null;
        int menosAtrasos = Integer.MAX_VALUE;

        // buscar empleado libre con menor retrasos
        for (IEmpleado e : empleadosLibres) {
            if (e == null) continue;
            int retrasos = e.obtenerRetrasos();
            if (retrasos < menorRetrasos) {
                menorRetrasos = retrasos;
                mejorCandidato = e;
            }
        }

        if (mejorCandidato == null) throw new Exception("No hay empleados disponibles");

        // remover de la estructura de libres y asignarlo
        empleadosLibres.remove(mejorCandidato);
        mejorCandidato.marcarAsignado(true);
        tarea.asignarEmpleado(mejorCandidato);
	}

	@Override
	public void registrarRetrasoEnTarea(Integer numero, String titulo, double cantidadDias)
			throws IllegalArgumentException {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");
        if (!tareas.containsKey(titulo))
            throw new RuntimeException("La tarea no está registrada");
        Tarea tarea = proyecto.buscarTarea(titulo);
        if (tarea == null) 
			throw new IllegalArgumentException("Tarea no encontrada");
        // Si la tarea tiene empleado asignado, incrementar su contador de retrasos.
        IEmpleado e = t.empleadoAsignado();
        if (e != null) {
            e.sumarRetraso();
        }
	}

	@Override
	public void agregarTareaEnProyecto(Integer numero, String titulo, String descripcion, double dias)
			throws IllegalArgumentException {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new IllegalArgumentException("El número de proyecto no está registrado.");
        if (proyecto.obtenerEstado().equals("FINALIZADO"))
            throw new IllegalArgumentException("No se puede agregar tareas a un proyecto finalizado.");
        if (tareas.containsKey(titulo))
            throw new IllegalArgumentException("El titulo: " + titulo + " ya esta registrado.");

        proyecto.agregarTarea(titulo, descripcion, dias);
	}

	@Override
	public void finalizarTarea(Integer numero, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new Exception("El número de proyecto no está registrado");

        // Validar tarea
        Tarea tarea = proyecto.buscarTarea(titulo);
        if (tarea == null) throw new Exception("Tarea no encontrada");

        tarea.finalizar();
	}

	@Override
	public void finalizarProyecto(Integer numero, String fin) throws IllegalArgumentException {
		Proyecto proyecto = proyectos.get(numero);
        if (proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");

        LocalDate fechaFin = LocalDate.parse(fin, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if(fechaFin.isBefore(proyecto.obtenerFechaInicio())) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        proyecto.finalizarProyecto(fin);

        // liberar empleados del proyecto y volver a agregarlos a la pila de libres
        for (Tarea t : p.getTareas()) {
            IEmpleado e = t.empleadoAsignado();
            if (e != null && !e.estaAsignado()) {
                empleadosLibres.push(e);
            } else if (e != null && e.estaAsignado()) {
                // si aún aparece asignado, lo liberamos y pusheamos
                e.marcarAsignado(false);
                empleadosLibres.push(e);
			}
        }
	}

	@Override
	public void reasignarEmpleadoEnProyecto(Integer numero, Integer legajo, String titulo) throws Exception {
		Proyecto proyecto = proyectos.get(numero);
        try {
            if(proyecto == null)
                throw new RuntimeException("El número de proyecto no está registrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        IEmpleado empleadoEnReemplazo = empleados.get(legajo);
        try {
            if(empleadoEnReemplazo == null)
                throw new RuntimeException("No existe ningun empleado con ese legajo");

            if(!empleadoEnReemplazo.estaLibre())
                throw new RuntimeException("Este empleado no esta libre");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        Tarea tarea = proyecto != null ? proyecto.obtenerTarea(titulo) : null;
        try {
            if (tarea == null)
                throw new Exception("La tarea no está registrada");

            if (!proyecto.perteneceAestaTarea(titulo))
                throw new Exception("La tarea no pertenece al proyecto");

            if (tarea.obtenerEmpleado() == null)
                throw new Exception("La tarea no tiene asignado ningun empleado previamente");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //Primero libero al empleado
        if (tarea != null) {
            tarea.liberarResponsable();
            tarea.asignarEmpleado(empleadoEnReemplazo);
        }
	}

	@Override
	public void reasignarEmpleadoConMenosRetraso(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = proyectos.get(numero);
		if(proyecto == null)
            throw new RuntimeException("El número de proyecto no está registrado");

        //Primero valido que este libre, despues en una primer instacia asigno al primer empleado libre
        //despues voy viendo cual es el que tiene menos retrasos
        Empleado empleadoConMenosRetraso = null;
        for(IEmpleado empleado : empleados.values() ){
            if(empleado.estaLibre()){
                if(empleadoConMenosRetraso == null ||
                        (empleado.obtenerRetrasos() < empleadoConMenosRetraso.obtenerRetrasos()))
                    empleadoConMenosRetraso = (Empleado) empleado;
            }
        }

        if(empleadoConMenosRetraso == null)
            throw new RuntimeException("No existe ningun empleado libre");

        Tarea tarea = proyecto.obtenerTarea(titulo);
        if (tarea == null)
            throw new Exception("La tarea no está registrada");

        if (!proyecto.perteneceAestaTarea(titulo))
            throw new Exception("La tarea no pertenece al proyecto");

        if (tarea.obtenerEmpleado() == null)
            throw new Exception("La tarea no tiene asignado ningun empleado previamente");

        //Primero libero al empleado
        tarea.liberarResponsable();

        //Ahora le reasigno al empleado con menos retrasos
        tarea.asignarEmpleado(empleadoConMenosRetraso);

	}

	@Override
	public double costoProyecto(Integer numero) {
		Proyecto p = proyectos.get(numero);
        if (p == null) return 0.0;

        double costoBase = 0.0;
        boolean tieneRetrasos = false;

        for (Tarea t : p.obtenerTareas()) {
            IEmpleado e = t.empleadoAsignado();
            if (e != null) {
                double dias = Math.ceil(t.getDiasEstimados());
                double costoTarea = e.calcularCosto(dias);

                // +2% para planta sin retrasos (según tu lógica)
                if (e instanceof EmpleadoDePlanta && e.obtenerLegajo() == 0) {
                    costoTarea *= 1.02;
                }

                if (e.obtenerRetrasos() > 0) {
                    tieneRetrasos = true;
                }

                costoBase += costoTarea;
            }
        }

        double factor = tieneRetrasos ? 1.25 : 1.35;
        return costoBase * factor;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosFinalizados() {
		List<Tupla<Integer, String>> finalizados = new ArrayList<>();
		for (Proyecto tupla : proyectos.values()) {
			if (tupla.verificarTodasTareasFinalizadas() && tupla.obtenerEstado().equals(Estado.finalizado)) {
				finalizados.add(new Tupla<>(tupla.obtenerCodigoProyecto(), tupla.obtenerDomicilio()));
			}
		}
		return finalizados;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosPendientes() {
		List<Tupla<Integer, String>> pendientes = new ArrayList<>();
        for (Proyecto tupla : proyectos.values()) {
            if (tupla.obtenerEstado().equals(Estado.pendiente)) {
                pendientes.add(new Tupla<>(tupla.obtenerCodigoProyecto(), tupla.obtenerDomicilio()));
            }
        }
        return pendientes;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosActivos() {
		List<Tupla<Integer, String>> activos = new ArrayList<>();
        Iterator<Map.Entry<Integer, Proyecto>> it = proyectos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Proyecto> entry = it.next();
            Proyecto p = entry.getValue();
            if (!p.isFinalizado() && Estado.activo.equals(p.obtenerEstado())) {
                activos.add(new Tupla<>(p.obtenerNumero(), p.obtenerDomicilio()));
            }
        }
        return activos;
	}

	@Override
	public Object[] empleadosNoAsignados() {
		List<Integer> legajos = new ArrayList<>();
        // iterar por la pila de libres; devolver legajos de los que no estén asignados
        for (IEmpleado emp : empleadosLibres) {
            if (emp != null && !emp.estaAsignado()) {
                legajos.add(emp.obtenerLegajo());
            }
        }
        return legajos.toArray();
	}

	@Override
	public boolean estaFinalizado(Integer numero) {
		Proyecto proyecto = proyectos.get(numero);
        return proyecto != null && proyecto.isFinalizado();
	}

	@Override
	public int consultarCantidadRetrasosEmpleado(Integer legajo) {
        IEmpleado e = empleados.get(legajo);
        return e != null ? e.obtenerRetrasos() : 0;
	}

	@Override
	public List<Tupla<Integer, String>> empleadosAsignadosAProyecto(Integer numero) {
		Proyecto p = proyectos.get(numero);
        if (p == null) return new ArrayList<>();

        Set<IEmpleado> unicos = new HashSet<>();
        List<Tupla<Integer, String>> resultado = new ArrayList<>();
        for (Tarea t : p.obtenerTareas()) {
            IEmpleado e = t.empleadoAsignado();
            if (e != null && unicos.add(e)) {
                resultado.add(new Tupla<>(e.obtenerLegajo(), e.obtenerNombre()));
            }
        }
        return resultado;
	}

	@Override
	public Object[] tareasProyectoNoAsignadas(Integer numero) {
		Proyecto p = proyectos.get(numero);
        if (p == null) return new Object[0];

        List<Tarea> lista = new ArrayList<>();
        Iterator<Tarea> it = p.obtenerTareas().iterator();
        while (it.hasNext()) {
            Tarea t = it.next();
            if (t.empleadoAsignado() == null) {
                lista.add(t);
            }
        }
        return lista.toArray();
	}

	@Override
	public Object[] tareasDeUnProyecto(Integer numero) {
		Proyecto p = proyectos.get(numero);
        if (p == null) return new Object[0];
        return p.obtenerTareas().toArray();
	}

	@Override
	public String consultarDomicilioProyecto(Integer numero) {
		return proyectos.get(numero).obtenerDomicilio();
	}

	@Override
	public boolean tieneRestrasos(Integer legajo) {
		IEmpleado e = empleados.get(legajo);
        return e != null && e.obtenerRetrasos() > 0;
	}

	@Override
	public List<Tupla<Integer, String>> empleados() {
		List<Tupla<Integer, String>> lista = new ArrayList<>();

        for(IEmpleado empleado: empleados.values()) {
            Integer legajo = empleado.obtenerLegajo();
            String nombre = empleado.obtenerNombre();
            lista.add(new Tupla<>(legajo, nombre));
        }
        return lista;
	}

	@Override
	public String consultarProyecto(Integer numero) {
		return proyectos.get(numero).mostrarInfo();
	}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("empleados:\n");
        for (IEmpleado e : empleados.values()) {
            sb.append(" ").append(e).append("\n");
        }
        sb.append(", \nproyectos\n");
        for (Proyecto p : proyectos.values()) {
            sb.append(" ").append(p).append("\n");
        }
        return sb.toString();
    }

    private void agregarEmpleadoLibre(IEmpleado empleado) {
        if(empleado.estaLibre() && !empleadosLibres.contains(empleado)) {
            empleadosLibres.add(empleado);
        }
    }
}

