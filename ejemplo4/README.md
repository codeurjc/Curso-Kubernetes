# Java + BBDD

## Sin persistencia ni secreto

- Desplegar

`$ kubectl create -f app.yaml -f mysql-deployment.yaml`

- Obtener la URL del Balanceador para acceder a la aplicación

`$ kubectl describe service/java-webapp-db-service`

- Eliminar

`$ kubectl delete -f app.yaml -f mysql-deployment.yaml`

## Persistencia para la Base de datos. Sin secreto

- Desplegar

`$ kubectl create -f app.yaml -f mysql-deployment-pvc.yaml`

- Obtener la URL del Balanceador para acceder a la aplicación

`$ kubectl describe service/java-webapp-db-service`

- Eliminar

`$ kubectl delete -f app.yaml -f mysql-deployment-pvc.yaml`

## Persistencia para la Base de datos. Con secreto para la contraseña de la base de datos.

- Desplegar

`$ kubectl create -f app-secret.yaml -f mysql-deployment-secret-pvc.yaml -f mysql-secret.yaml`

- Obtener la URL del Balanceador para acceder a la aplicación

`$ kubectl describe service/java-webapp-db-service`

- Eliminar

`$ kubectl delete -f app-secret.yaml -f mysql-deployment-secret-pvc.yaml -f mysql-secret.yaml`

## Chart para desplegar con Helm

Descargamos el repo:

`$ git clone https://github.com/codeurjc/Curso-Kubernetes`

Entramos al directorio de trabajo

`$ cd Curso-Kubernetes/ejemplo4`

Y ejecutamos

`$ helm install --name java-db ./java-bd`

Se siguen las indicaciones en pantalla que están preparadas para mostrar la URL a la que te tienes que conectar.