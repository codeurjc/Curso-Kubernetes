# Curso-Kubernetes

## Ejemplo 1

Para usar este ejemplo en **minikube** seguimos estos pasos (suponemos dispones de un minikube corriendo):

1. Crear el `Deployment` y el `Service`

`$ kubectl create -f webgatos-minikube.yaml`

2. Esperar a que el `Deployment` esté listo. Tiene que descargar la imagen.

`kubectl get deployments`

Tenemos que verlo **running**

```
NAME              DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
webgatos-deploy   1         1         1            1           10m
```

3. Vemos el **service**

`kubectl get svc`

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

1. Crear el background Mongo

	1.1 Crear la clave compartida para Mongo como secreto de Kubernetes

	```
	TMPFILE=$(mktemp)
	/usr/bin/openssl rand -base64 741 > $TMPFILE
	kubectl create secret generic shared-bootstrap-data --from-file=internal-auth-mongodb-keyfile=$TMPFILE
	rm $TMPFILE
	```

	1.2 Crear el StatefulSet

	`kubectl apply -f mongodb-service.yaml`

Esperar hasta que las 3 replicas aparezcan como **running**

`kubectl get pods`

```
NAME                                  READY     STATUS    RESTARTS   AGE       
mongod-0                              1/1       Running   0          7m       
mongod-1                              1/1       Running   0          7m       
mongod-2                              1/1       Running   0          7m       
```

2. Inicializamos el Replica Set

Basicamente pasamos al primario la dirección de los demás nodos que formaran el cluster.

```
kubectl exec mongod-0 -c mongod-container -- mongo --eval 'rs.initiate({_id: "MainRepSet", version: 1, members: [ {_id: 0, host: "mongod-0.mongodb-service.default.svc.cluster.local:27017"}, {_id: 1, host: "mongod-1.mongodb-service.default.svc.cluster.local:27017"}, {_id: 2, host: "mongod-2.mongodb-service.default.svc.cluster.local:27017"} ]});'
```

3. Esperamos a que se configure (30 segundos aprox). Comprobamos:

`kubectl exec mongod-0 -c mongod-container -- mongo --eval 'rs.status();'`

Debemos ver una salida enumerando los miembros del cluster.

4. Crear la contraseña para el administrador.

```
export PASS=abc123
kubectl exec mongod-0 -c mongod-container -- mongo --eval 'db.getSiblingDB("admin").createUser({user:"main_admin",pwd:"'"${PASS}"'",roles:[{role:"root",db:"admin"}]});'
```

Esta password está también definida en nuestra aplicación y se la pasamos por variable de entorno.

5. Desplegar la aplicación. Si no hemos cambiado la contraseña anterior podemos lanzarlo directamente

`kubectl create -f python-mongo-minikube.yaml`

Si hemos cambiado la contraseña, editar el _yaml_ y actualizar el parámetro.

6. Acceder al servicio

`minikube service python-mongo-service`

7. Podemos escalar el front python de la aplicación

`kubectl scale deployment python-mongo-deploy --replicas=3`