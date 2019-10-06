# Image Trust: Docker regisrty, Notary y Portieris

* https://www.youtube.com/watch?v=JK70k_B87mw
* https://github.com/IBM/portieris


## Notary

https://github.com/theupdateframework/notary

Notary es una herramienta para firmar imágenes Docker y para verificar las firmas. Para ello, firma los hash de los ficheros de las imágenes. De esa forma, un  cliente puede verificar si el contenido de una imangen es el contenido que el autor generó y la imagen no ha sido manipulada. 

Se puede instalar de forma independiente o junto con el registro de imágenes. DockerHub, Artifactory, Azure registry tienen Notary integrado.

## Control de firmas

Docker se puede configurar para que sólo pueda ejecutar las imágenes firmadas. Pero esa configuración es a nivel de Docker y por tanto debe hacerse en los nodos del cluster. No se puede controlar a nivel de Kubernetes.

Para poder controlar en Kubernetes qué imágenes deberían tener un control sobre su firma (y por tanto su procedencia), IBM ha creado [Portieris](https://github.com/IBM/portieris). 

Portieris es una herramienta implementada como Admission Controller que verifica la firma de las pod que va a ejecutar. Además, despues de verificar la imagen, sustituye la referencia a la imagen en el pod spec por su hash, de forma que la imagen no puede ser cambiada en el registro.

## Problemas encontrados

Parece que Portieries no funciona con un repositorio privado que no sea el de BluMix

