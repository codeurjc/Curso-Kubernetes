Tareas
===

# Desplegar ejemplos del tema de Docker en Kubernetes

## Aplicación web python (un contenedor) (slide 78)

Creado el YAML con la definición del deployment y service en webgatos.yaml.

Es una aplicación bastante simple que mete en un contenedor docker un código Python y lo ejecuta.


## Aplicación web python con BBDD Mongo (dos contenedores, docker-compose)

- En los ficheros mongo.yaml y python-mongo.yaml hemos definido una aplicación Python que se conecta a una base de datos Mongo. El Mongo tarda en arrancar y da timeout desde el front a la conexión de la BBDD.

## Volumen de MongoDB

## Escalado de la web en kubernetes (replicas del pod)



# Cómo desplegar Kubernetes en AWS

## Formatos: CloudFormation (heptio), EKS, otros?

- kops
- heptio
- kubeadm

## Kubernetes en Cloud

### Load Balancer es compartido entre todos los servicios

### Volumenes > EBS

### Otro integración? 

### Autoscaling de AWS con workers Kubernetes

* scale-in

* scale-out

El cluster crece y decrece usando las herramientas de autoescalado de AWS. Al decrecer vuelve a recrear los contenedores en los nodos que queden disponibles. Hay interrupción del servicio el tiempo que tarda kubernetes en darse cuenta que ha perdido el nodo (15 - 20 segundos en un cluster de 3 nodos)

Avisar de decrecimiento? -> 

Con kubectl drain $NODE se etiqueta el node como en mantenimiento y automáticamente pasa todos los contenedores a un nodo disponible.

Con kubectl uncordon $NODE el nodo vuelve a estar disponible pero el Scheduler no devuelve los contenedores a su nodo, es decir, el nodo está disponible y vacio.

Hay un kubectl cordon $NODE que marca el nodo como en mantenimiento pero no lo evacua.

# Desplegar apps “cloud-native”

## JHipster

* Versión monolítica en Kubernetes

* Versión microservicios en Kubernetes

* Demo de https://github.com/codeurjc/SquirrelDrey (limitado porque tenemos que saber cómo configurar hazelcast para Kubernets).

* FullTeaching

# Misc

## Logging

- Centralizar los logs de los pods usando Fluentd + EKL

## Auth

- Nos podemos autenticar frente al API del kubernetes con certificados X509 que es por defecto o

- Basic auth
- Proxy
- OpenID

## Authorization

- Al crear el cluster con kops es recomendable pasar el flag --authorization para que configure los roles de los elementos.

- Si no se ha hecho, añadir el flag --authorization-mode=RBAC al api server

- RBAC

	- Crear roles y asignar a los usuarios o grupos
	- El permisos puede ser para un namespace espcifico o para todos
	