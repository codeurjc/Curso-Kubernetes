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

