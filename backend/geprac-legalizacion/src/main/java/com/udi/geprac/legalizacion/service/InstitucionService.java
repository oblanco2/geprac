package com.udi.geprac.legalizacion.service;

import com.udi.geprac.legalizacion.domain.ContactoInstitucion;
import com.udi.geprac.legalizacion.domain.Institucion;
import com.udi.geprac.legalizacion.dto.ContactoDto;
import com.udi.geprac.legalizacion.dto.InstitucionDto;
import com.udi.geprac.legalizacion.repository.InstitucionRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU-03 · Gestionar instituciones receptoras. Una institución con
 * inscripciones no se elimina: para retirarla se marca inactiva, y así deja de
 * ofrecerse al estudiante sin perder lo que ya se inscribió en ella.
 *
 * @author Darien Asdrwal Pesca Ojeda
 */
@Service
@Transactional
public class InstitucionService {

    private final InstitucionRepository instituciones;

    public InstitucionService(InstitucionRepository instituciones) {
        this.instituciones = instituciones;
    }

    /** Paso 2: el catálogo, por razón social. */
    @Transactional(readOnly = true)
    public List<InstitucionDto> listarInstituciones() {
        return instituciones.findAllByOrderByRazonSocial().stream().map(InstitucionDto::de).toList();
    }

    /** Pasos 7 a 10: comprueba que no exista ya en esa ciudad y la guarda con sus contactos. */
    public InstitucionDto crearInstitucion(InstitucionDto datos) {
        if (instituciones.existsByRazonSocialAndCiudad(datos.razonSocial().trim(), datos.ciudad().trim()))
            throw new IllegalStateException("Ya existe «" + datos.razonSocial().trim() + "» en " + datos.ciudad().trim() + ".");
        Institucion i = new Institucion();
        copiar(datos, i);
        return InstitucionDto.de(instituciones.save(i));
    }

    /** Corrige una institución y sus contactos. */
    public InstitucionDto actualizarInstitucion(Long id, InstitucionDto datos) {
        Institucion i = instituciones.findById(id)
            .orElseThrow(() -> new NoSuchElementException("La institución no existe."));
        boolean cambiaNombre = !i.getRazonSocial().equals(datos.razonSocial().trim()) || !i.getCiudad().equals(datos.ciudad().trim());
        if (cambiaNombre && instituciones.existsByRazonSocialAndCiudad(datos.razonSocial().trim(), datos.ciudad().trim()))
            throw new IllegalStateException("Ya existe «" + datos.razonSocial().trim() + "» en " + datos.ciudad().trim() + ".");
        copiar(datos, i);
        try {
            Institucion guardada = instituciones.save(i);
            instituciones.flush();
            return InstitucionDto.de(guardada);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Uno de los contactos que quitó figura en inscripciones: no se puede quitar.");
        }
    }

    /** Elimina una institución que no tiene inscripciones. */
    public void eliminarInstitucion(Long id) {
        if (!instituciones.existsById(id)) throw new NoSuchElementException("La institución no existe.");
        try {
            instituciones.deleteById(id);
            instituciones.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("La institución tiene inscripciones asociadas: no se puede eliminar. "
                + "Para retirarla del catálogo, márquela como inactiva.");
        }
    }

    // ------------------------------------------------------------------ apoyo

    private static void copiar(InstitucionDto d, Institucion i) {
        i.setRazonSocial(d.razonSocial().trim());
        i.setNit(texto(d.nit()));
        i.setDireccion(d.direccion().trim());
        i.setCiudad(d.ciudad().trim());
        i.setTelefono(d.telefono().trim());
        i.setCorreo(texto(d.correo()));
        i.setSitioWeb(texto(d.sitioWeb()));
        i.setRepresentanteLegal(texto(d.representanteLegal()));
        i.setActiva(d.activa());
        // los contactos que llegan con id se corrigen; los nuevos se añaden; los que faltan se quitan
        List<Long> conservados = d.contactos().stream().map(ContactoDto::id).filter(Objects::nonNull).toList();
        i.getContactos().removeIf(c -> c.getId() != null && !conservados.contains(c.getId()));
        for (ContactoDto c : d.contactos()) {
            ContactoInstitucion contacto = c.id() == null ? null
                : i.getContactos().stream().filter(x -> c.id().equals(x.getId())).findFirst().orElse(null);
            if (contacto == null) {
                contacto = new ContactoInstitucion(i);
                i.getContactos().add(contacto);
            }
            contacto.setNombre(c.nombre().trim());
            contacto.setCargo(c.cargo().trim());
            contacto.setTelefono(c.telefono().trim());
            contacto.setCelular(texto(c.celular()));
            contacto.setCorreo(texto(c.correo()));
        }
    }

    private static String texto(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
