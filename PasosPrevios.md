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

`$ minikube ssh` 

## Probar que funciona correctamente

Mostrar la consola visual de Kubernetes

`$ minikube dashboard` 

Después de unos segundos, se debería abrir un navegador web automáticamente con un dashboard de Kubernetes similar a [este](https://labs.consol.de/assets/2017-02-10-minikube/kubernetes_dashboard.png).


