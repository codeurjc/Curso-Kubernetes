# Curso-Kubernetes

* Ejemplo 1: Web gatos Python sin BBDD
* Ejemplo 2: Web anuncios Python con Mongo y Java con MySQL
* Ejemplo 3: Web número Spring con PostgreSQL

## Ejemplo 1

Para usar este ejemplo en **minikube** seguimos estos pasos (suponemos dispones de un minikube corriendo):

1. Crear el `Deployment` y el `Service`

`$ kubectl create -f webgatos-minikube.yaml`

2. Esperar a que el `Deployment` esté listo. Tiene que descargar la imagen.

`$ kubectl get deployments`

Tenemos que verlo **running**

```
NAME              DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
webgatos-deploy   1         1         1            1           10m
```

3. Vemos el **service**

`$ kubectl get svc`

```
NAME               TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
webgatos-service   NodePort    10.100.93.166   <none>        5000:32615/TCP   11m
```

4. Accedemos al servicio

`minikube service webgatos-service`

Esto nos abrirá el browser para que podamos ver la aplicación corriendo.

5. Borramos la aplicación al terminar

`kubectl delete -f webgatos-minikube.yaml`

## Ejemplo 2

Para usar este ejemplo en **minikube** seguimos estos pasos (suponemos dispones de un minikube corriendo):

### Java con MySQL

Este ejemplo es la versión fácil de la aplicación ya que usamos _Development_. Lo ideal con Bases de datos es usar _StatefulSets_ como veremos en el siguiente ejemplo.

Para desplegar esta aplicación simplemente:

> Sin persistencia para la base de datos:

`$ kubectl create -f mysql-service-without-pvc.yaml`

> Con persistencia para la base de datos:

`$ mysql-service-without-pvc.yaml`

Y luego la aplicación:

`$ java-mysql-minikube.yaml`

### Python con MongoDB

Este ejemplo es de nivel más avanzado ya que hace uso de _Statefulsets_ para crear la Base de datos.

1. Crear el background Mongo

	1.1 Crear la clave compartida para Mongo como secreto de Kubernetes

	```
	$ TMPFILE=$(mktemp)
	$ /usr/bin/openssl rand -base64 741 > $TMPFILE
	$ kubectl create secret generic shared-bootstrap-data --from-file=internal-auth-mongodb-keyfile=$TMPFILE
	$ rm $TMPFILE
	```

	1.2 Crear el StatefulSet

	> Sin persistencia de los datos de la base de datos

	`$ kubectl apply -f mongodb-service-without-pvc.yaml`

	> Con persistencia de los datos de la base de datos

	`$ kubectl apply -f mongodb-service-with-pvc.yaml`

Esperar hasta que las 3 replicas aparezcan como **running**

`$ kubectl get pods`

```
NAME                                  READY     STATUS    RESTARTS   AGE       
mongod-0                              1/1       Running   0          7m       
mongod-1                              1/1       Running   0          7m       
mongod-2                              1/1       Running   0          7m       
```

2. Inicializamos el Replica Set

Basicamente pasamos al primario la dirección de los demás nodos que formaran el cluster.

```
$ kubectl exec mongod-0 -c mongod-container -- mongo --eval 'rs.initiate({_id: "MainRepSet", version: 1, members: [ {_id: 0, host: "mongod-0.mongodb-service.default.svc.cluster.local:27017"}, {_id: 1, host: "mongod-1.mongodb-service.default.svc.cluster.local:27017"}, {_id: 2, host: "mongod-2.mongodb-service.default.svc.cluster.local:27017"} ]});'
```

3. Esperamos a que se configure (30 segundos aprox). Comprobamos:

`$ kubectl exec mongod-0 -c mongod-container -- mongo --eval 'rs.status();'`

Debemos ver una salida enumerando los miembros del cluster.

4. Crear la contraseña para el administrador.

```
$ export PASS=abc123
$ kubectl exec mongod-0 -c mongod-container -- mongo --eval 'db.getSiblingDB("admin").createUser({user:"main_admin",pwd:"'"${PASS}"'",roles:[{role:"root",db:"admin"}]});'
```

Esta password está también definida en nuestra aplicación y se la pasamos por variable de entorno.

Este comando lo ejecutamos en el pod que es PRIMARY del cluster. Si obtienes un error 

`Error: couldn't add user: not master :`

Significa que no estás en el PRIMARY. Consulta la salida en la que hemos visto la lista de miembros del cluster para conectar al que es PRIMARY.

5. Desplegar la aplicación. Si no hemos cambiado la contraseña anterior podemos lanzarlo directamente

`$ kubectl create -f python-mongo-minikube.yaml`

Si hemos cambiado la contraseña, editar el _yaml_ y actualizar el parámetro.

6. Acceder al servicio

`$ minikube service python-mongo-service`

7. Podemos escalar el front python de la aplicación

`$ kubectl scale deployment python-mongo-deploy --replicas=3`

## Ejemplo 3

En este ejemplo veremos como desplegar una base de datos Postgres contra una aplicación SpringBoot.

*Nota*: Esta aplicación funciona igual en minikube y AWS.

1. Comenzamos desplegando la base de datos:

`$ kubectl create -f postgres.yml`

2. Crear un config map con _hostname_ de Postgress:

`$ kubectl create configmap hostname-config --from-literal=postgres_host=$(kubectl get svc postgres -o jsonpath="{.spec.clusterIP}")`

3. Creamos la aplicación SpringBoot

`$ kubectl create -f specs/spring-boot-app.yml`

4. Si estamos en *minikube* podemos acceder al servicio:

`$ minikube service spring-boot-postgres-sample`

que nos abrirá un navegador web con la página.

5. En cambio si estamos en AWS debemos acceder a través del load balancer. 

`$ kubectl describe service/spring-boot-postgres-sample`

donde veremos:

```
Name:                     spring-boot-postgres-sample
Namespace:                default
Labels:                   app=spring-boot-postgres-sample
Annotations:              <none>
Selector:                 app=spring-boot-postgres-sample
Type:                     LoadBalancer
IP:                       10.109.206.92
LoadBalancer Ingress:     ac433f0d95dac11e8a3430a3fbd5fbd3-1384713647.eu-west-1.elb.amazonaws.com
Port:                     <unset>  8080/TCP
TargetPort:               8080/TCP
NodePort:                 <unset>  32542/TCP
Endpoints:                172.17.0.10:8080,172.17.0.7:8080,172.17.0.9:8080
Session Affinity:         None
External Traffic Policy:  Cluster
Events:                   <none>
  Type    Reason                Age   From                Message
  ----    ------                ----  ----                -------
  Normal  EnsuringLoadBalancer  1m    service-controller  Ensuring load balancer
  Normal  EnsuredLoadBalancer   1m    service-controller  Ensured load balancer
```

Accederemos al servicio a través de la url del campo _LoadBalancer Ingress_
