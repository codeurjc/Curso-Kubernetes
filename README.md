# Curso-Kubernetes

Material que se uso en el Curso de Kubernetes impartido por CodeURJ.

## Ejercicio 1

1. Despliega en Kubernetes una aplicación web “webgatos”

```
Imagen:  codeurjc/webgatos:v1
Puerto: 5000
Código de la aplicación: Apps/web-gatos
```

2. Crea un servicio para ella
3. Accede a ella desde el navegador web

## Ejercicio 2

Escala el deployment de webgatos para que tenga dos réplicas

Comprueba que si se crean esas réplicas

Verifica que al acceder a la web cada vez se obtiene una IP diferente porque se accede a un contenedor diferente

## Ejercicio 3

Despliega una aplicación de web de anuncios con base de datos en Kubernetes

**Aplicación Web**

```
Imagen: codeurjc/java-webapp-bbdd:v2
Puerto: 8080
Variables de entorno: 
MYSQL_ROOT_PASSWORD = pass
MYSQL_DATABASE = test
```

**Base de datos:**

```
Imagen: mysql:5.6
Puerto: 3306
Nombre del servicio: db
Variables de entorno
MYSQL_ROOT_PASSWORD=pass
MYSQL_DATABASE=test
```

## Ejercicio 4

**Despliega una web con BBDD persistente**

- Guarda los datos de la BBDD del Ejercicio 3 en un volumen persistente
- Podemos usar el mismo Persistence Volume creado previamente (/mnt/data)
- La ruta en la que MySQL guarda los datos (mountPath) es: /var/lib/mysql

## Ejercicio 5

Crea un chart para la web de anuncios con base de datos


