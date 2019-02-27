# Pasos previos a la sesión

Por favor, sigue estos pasos antes de la sesión para que sea más ágil comenzar. En resumen, de lo que se trata es de tener la instancia de Minikube con las imágenes Docker descargadas para que sea más rápido empezar.

## Instala minikube

Instala minikube siguiente las instrucciones de esta página. Como hypervisor instala VirtualBox.

https://kubernetes.io/docs/tasks/tools/install-minikube/

## Arranca Minikube

Al finalizar la instalación arranca Minikube con al menos 4GB de RAM y 4CPUs

`$ minikube start --memory=4098 --cpus=4`

## Conenctate a Minikube y descarga contenedores

Para conectarnos a la máquina virtual que corre Minikube hacemos:

`$ minikube ssh` 

Con este comando descargaras las imágenes Docker dentro de la máquina virtual que corre Minikube para que estén disponibles durante el curso (tardará unos minutos, paciencia).

`curl -s https://raw.githubusercontent.com/codeurjc/Curso-Kubernetes/master/docker-pull.sh | bash`

Cerramos la conexión ssh con la máquina virtual de minikube

`$ exit` 

## Probar que funciona correctamente

Mostrar la consola visual de Kubernetes

`$ minikube dashboard` 

Después de unos segundos, se debería abrir un navegador web automáticamente con un dashboard de Kubernetes similar a [este](https://labs.consol.de/assets/2017-02-10-minikube/kubernetes_dashboard.png).

## Parar la máquina virtual

Ejecutamos el comando.

`$ minikube stop` 

Si abrimos el interfaz gráfico de Virtualbox podemos ver la máquina virtual con minikube.

## Descargar el binario de Helm

Descarga desde [aquí](https://github.com/helm/helm/releases) el binario correspondiente a tu arquitectura. Observa de bajarte un versión estable, que son las que aparecen con una etiqueta verde a la izquierda que dice _latest release_. Hay versiones para Linux, Windows y Mac.

Las instrucciones de instalación puedes encotrarlas [aquí](https://helm.sh/docs/using_helm/#installing-helm)

