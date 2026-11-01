/**
 * Las preguntas frecuentes de cada rol, con sus respuestas: las del estudiante,
 * las del tutor académico y las del director del programa, agrupadas por tema,
 * y las comunes al ingreso y al uso del software. Cada respuesta dice lo que el
 * software hace, con los nombres de sus pantallas y botones; ir lleva a la
 * opción del menú de la que habla, y estados presenta los cinco estados de una
 * inscripción con su significado.
 *
 * @author José Fernando Rincón Barrios
 */

/** Las tablas paginadas: el tutor y el director las tienen. */
const TABLAS = {
  p: '¿Por qué las tablas muestran solo cinco filas?',
  r: 'Para que la pantalla no se desplace de más: cada página muestra cinco. Al pie de la tabla están la paginación y el '
    + 'filtro por estado, con el número de filas de cada uno; es el mismo filtro de la lista «Estado» de arriba.',
}

export const PREGUNTAS = {
  ESTUDIANTE: [
    {
      titulo: 'Mi perfil y mi hoja de vida',
      preguntas: [
        {
          p: '¿Qué debo diligenciar antes de inscribir mi práctica?',
          r: 'Sus datos personales en «Mi perfil» y su hoja de vida en «Mi hoja de vida». La inscripción copia esos datos y '
            + 'los cuatro formatos los exigen: si falta alguno, el software le dice cuál y lo lleva a la pantalla donde se '
            + 'diligencia.',
          ir: ['/mi-perfil', 'Mi perfil'],
        },
        {
          p: '¿Qué indica el avance del perfil?',
          r: 'Cuánto lleva diligenciado de lo que exigen los cuatro formatos, y qué le falta. Para guardar el registro bastan '
            + 'nombres, apellidos, tipo y número de documento, programa y semestre; para inscribir la práctica, el perfil '
            + 'debe estar completo.',
        },
        {
          p: '¿Debo actualizar mi perfil cada semestre?',
          r: 'Solo lo que haya cambiado: el registro se conserva de un semestre a otro. Mantenga al día el semestre que cursa, '
            + 'porque decide qué práctica le corresponde inscribir.',
        },
        {
          p: '¿Por qué no puedo cambiar mi correo institucional?',
          r: 'Porque viene de su cuenta: el software lo toma de ella y no lo edita.',
        },
        {
          p: '¿Cuántas referencias personales puedo registrar?',
          r: 'Hasta tres, el máximo que admite el formato PR-01 de la hoja de vida.',
          ir: ['/mi-hoja-de-vida', 'Mi hoja de vida'],
        },
      ],
    },
    {
      titulo: 'La inscripción de la práctica',
      preguntas: [
        {
          p: '¿Qué práctica me corresponde inscribir?',
          r: 'La del semestre que cursa, según el programa y el semestre que registró en «Mi perfil», dentro del semestre '
            + 'académico que abrió la Dirección del Programa. El software la prepara con su contenido: no se escoge de una '
            + 'lista.',
          ir: ['/inscribir', 'Inscribir práctica'],
        },
        {
          p: '¿Por qué no puedo inscribir mi práctica?',
          r: 'Puede faltar algún dato de su perfil o de su hoja de vida, y el software le dice cuál. También puede ser que la '
            + 'Dirección del Programa todavía no haya abierto el semestre, o que no tenga cargada o activa la práctica de su '
            + 'semestre.',
        },
        {
          p: '¿Cuáles son los pasos de la inscripción?',
          r: 'Tres. En el primero escoge la institución receptora; en el segundo, el contacto que lo acompañará y las fechas; en '
            + 'el tercero revisa el contenido precargado, ajusta su dirección, su celular y su correo personal, y la envía con '
            + '«Enviar a revisión». Lo de cada paso se guarda, y con «Guardar y salir» la retoma después con lo que guardó.',
        },
        {
          p: '¿Puedo cambiar la inscripción después de enviarla?',
          r: 'No: al enviarla pasa al tutor académico designado y deja de admitir cambios. Si el tutor la devuelve, puede '
            + 'corregirla y reenviarla sin volver a empezar.',
        },
        {
          p: '¿Qué hago si el tutor devolvió mi inscripción?',
          r: 'En «Estado de mi inscripción» está el motivo que registró. Con «Corregir inscripción» ajuste lo que pide —el '
            + 'motivo lo acompaña en cada paso— y envíela de nuevo con «Reenviar a revisión».',
          ir: ['/mi-inscripcion', 'Estado de mi inscripción'],
        },
        {
          p: '¿Dónde veo cómo va mi inscripción?',
          r: 'En «Estado de mi inscripción»: su estado y lo que significa, los datos que la identifican y su historial, con el '
            + 'envío, cada revisión y el aval.',
        },
      ],
    },
    {
      titulo: 'Mis formatos y mis prácticas',
      preguntas: [
        {
          p: '¿Cuándo puedo obtener mis formatos?',
          r: 'Cuando la Dirección del Programa avale su inscripción. Antes, «Mis formatos» los presenta deshabilitados, con el '
            + 'estado en que va.',
          ir: ['/mis-formatos', 'Mis formatos'],
        },
        {
          p: '¿Qué formatos emite el software?',
          r: 'Los cuatro institucionales, en PDF y ya diligenciados con sus datos: PR-01 Hoja de vida del estudiante, PR-02 '
            + 'Acta de conocimiento de los términos, PR-04 Solicitud y aprobación de práctica y PR-05 Acta de compromiso.',
        },
        {
          p: '¿Puedo generarlos de nuevo?',
          r: 'Sí, con «Generar de nuevo»: se emiten otra vez con la plantilla vigente y reemplazan a los anteriores. Cada uno se '
            + 'descarga tal como se emitió.',
        },
        {
          p: '¿Qué muestra «Mis prácticas»?',
          r: 'Las ocho prácticas de su plan: las inscritas, con su estado, y las demás como no inscritas. Es su historial y no '
            + 'modifica ningún dato.',
          ir: ['/mis-practicas', 'Mis prácticas'],
        },
      ],
    },
  ],
  TUTOR: [
    {
      titulo: 'La revisión de las inscripciones',
      preguntas: [
        {
          p: '¿Qué inscripciones llegan a mi bandeja?',
          r: 'Las de las prácticas que la Dirección del Programa le designó en el semestre abierto. La bandeja abre con las '
            + 'enviadas, las que esperan su revisión, de la que lleva más tiempo esperando a la más reciente.',
          ir: ['/bandeja', 'Bandeja de revisión'],
        },
        {
          p: '¿Por qué no veo inscripciones en mi bandeja?',
          r: 'Puede que la Dirección del Programa todavía no le haya designado prácticas en el semestre abierto, o que ningún '
            + 'estudiante haya enviado aún la suya. Revise también los filtros: la bandeja abre con las enviadas.',
        },
        {
          p: '¿Cómo apruebo o devuelvo una inscripción?',
          r: 'Con «Revisar», en su fila, se abren los datos de la inscripción, el contenido precargado y la institución '
            + 'receptora. Al final están «Aprobar», que la pasa al aval de la Dirección del Programa, y «Devolver», que se la '
            + 'regresa al estudiante para que la corrija.',
        },
        {
          p: '¿Es obligatorio el motivo de la devolución?',
          r: 'Sí: sin motivo la inscripción no se devuelve. Admite hasta 600 caracteres, y el estudiante lo ve en el estado de '
            + 'su inscripción y en cada paso, hasta que la reenvíe.',
        },
        {
          p: '¿Puedo cambiar una revisión que ya hice?',
          r: 'No: una inscripción aprobada, devuelta o avalada solo se consulta. Si el estudiante corrige y reenvía una '
            + 'devuelta, vuelve a su bandeja como enviada.',
        },
      ],
    },
    {
      titulo: 'El historial y las tablas',
      preguntas: [
        {
          p: '¿Qué muestra «Historial de mis prácticas»?',
          r: 'Las inscripciones de las prácticas que ha tenido designadas, con filtros por estudiante, práctica, institución, '
            + 'semestre y estado. «Exportar historial» descarga en CSV las que dejan los filtros.',
          ir: ['/historial', 'Historial de mis prácticas'],
        },
        TABLAS,
      ],
    },
  ],
  DIRECTOR: [
    {
      titulo: 'La preparación del semestre',
      preguntas: [
        {
          p: '¿Qué hay que preparar al comenzar el semestre?',
          r: 'En «Preparación del semestre»: abrir el semestre, asignar el rol de cada cuenta y designar el tutor académico de '
            + 'cada práctica. Además, las prácticas deben estar activas en el «Catálogo de prácticas», y las instituciones, en '
            + '«Instituciones receptoras».',
          ir: ['/semestre', 'Preparación del semestre'],
        },
        {
          p: 'Una persona no puede entrar porque su cuenta no tiene rol. ¿Qué hago?',
          r: 'Asígnele el rol en «Preparación del semestre», en la tabla «Roles de los usuarios», con «Asignar». La cuenta '
            + 'aparece allí después de su primer intento de ingreso con el correo institucional.',
        },
        {
          p: '¿Por qué no puedo quitarle el rol de tutor a alguien?',
          r: 'Porque tiene prácticas designadas en el semestre abierto. Primero reasígnelas a otro tutor, en la tabla del tutor '
            + 'de cada práctica.',
        },
        {
          p: '¿Qué pasa al cerrar el semestre?',
          r: 'Ningún estudiante puede inscribir su práctica mientras no se abra otro, y las designaciones de los tutores se '
            + 'consultan pero no se cambian. Solo puede haber un semestre abierto a la vez.',
        },
      ],
    },
    {
      titulo: 'Los catálogos',
      preguntas: [
        {
          p: '¿Por qué no puedo eliminar una práctica o una institución?',
          r: 'Porque ya tiene inscripciones. Para retirarla, márquela inactiva: deja de ofrecerse, y las inscripciones que ya '
            + 'la tienen no cambian.',
        },
        {
          p: 'Si edito una práctica, ¿cambian las inscripciones que ya existen?',
          r: 'No: cada inscripción guarda su propia copia del contenido. Los cambios valen para las inscripciones que se creen '
            + 'después.',
          ir: ['/practicas', 'Catálogo de prácticas'],
        },
        {
          p: '¿Para qué sirve el contacto de una institución?',
          r: 'Es quien figura como tutor del escenario en el formato de solicitud y aprobación (PR-04). El estudiante lo escoge '
            + 'en el segundo paso de su inscripción, así que cada institución activa debe tener al menos uno.',
          ir: ['/instituciones', 'Instituciones receptoras'],
        },
      ],
    },
    {
      titulo: 'El aval y el historial',
      preguntas: [
        {
          p: '¿Qué inscripciones llegan a la bandeja de aval?',
          r: 'Las de su programa que el tutor académico ya aprobó: ninguna llega sin esa revisión. Después de las pendientes '
            + 'aparecen las ya avaladas del semestre, para consultarlas.',
          ir: ['/aval', 'Bandeja de aval'],
        },
        {
          p: '¿Qué pasa al avalar una inscripción?',
          r: 'El software le habilita al estudiante la emisión de sus cuatro formatos institucionales. Una inscripción avalada '
            + 'ya no ofrece la opción de avalar: solo se consulta.',
        },
        {
          p: '¿Qué muestra «Historial del programa»?',
          r: 'Todas las inscripciones de su programa, con filtros por estudiante, práctica, institución, semestre y estado. '
            + '«Exportar historial» descarga en CSV las que dejan los filtros.',
          ir: ['/historial', 'Historial del programa'],
        },
        TABLAS,
      ],
    },
  ],
}

/** Las de todos los roles: van al final, después de las del rol. */
export const COMUNES = {
  titulo: 'El ingreso y el uso del software',
  preguntas: [
    {
      p: '¿Con qué datos ingreso a GEPRAC?',
      r: 'Con su correo institucional (usuario@udi.edu.co) y su contraseña. El rol de su cuenta decide qué opciones ve en el '
        + 'menú.',
    },
    {
      p: '¿Qué hago si olvidé mi contraseña?',
      r: 'El restablecimiento de la contraseña lo resuelve el proveedor de identidad institucional, no el software.',
    },
    {
      p: '¿Por qué la primera conexión puede tardar?',
      r: 'Si los servicios llevaban un rato sin uso, la primera petición puede tardar hasta un minuto mientras se activan; '
        + 'mientras tanto, el software muestra un aviso de espera. Las siguientes responden con normalidad.',
    },
    {
      p: '¿Cómo cierro la sesión o entro con otra cuenta?',
      r: 'Con «Cerrar sesión», arriba a la derecha. Después puede entrar con otra cuenta, del rol que sea, en la misma '
        + 'pestaña y sin cerrar el navegador.',
    },
    {
      p: '¿Por qué el software me pregunta antes de salir de una pantalla?',
      r: 'Porque en ella hay cambios sin guardar. Si confirma la salida, esos cambios se pierden; si cierra o recarga la '
        + 'pestaña, el navegador también le pregunta.',
    },
    {
      p: '¿Qué significa cada estado de una inscripción?',
      r: 'La inscripción de cada práctica pasa por estos estados:',
      estados: true,
    },
    {
      p: '¿Puedo usar GEPRAC desde el celular?',
      r: 'Sí. Las pantallas se ajustan al ancho del celular, y las tablas se presentan como tarjetas, con el nombre de cada '
        + 'dato.',
    },
  ],
}
