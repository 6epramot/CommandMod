# CommandMod
Это мод с открытым кодом на майнкрафт 1.7.10
Сам проект был выполнен с помощью @GTNewHorizons стартера.
Рекомендую gradle файл стартера, очень быстро настроил и приступил к работе.

Сам мод доступен на Modrinth https://modrinth.com/mod/commandmod. Также файл мода есть на моём тематическом дискорд сервере https://discord.gg/7Q7hVZGm.
Там же можно будет поиграть в режим Glassbreakers, который работает с помощью этого мода.

Мод добавляет возможность создавать точку для установки флага на определённых координатах(с помощью команды /flagpoint x y z), из блока на данных координатах в небо устремится белый луч, обозначающий его место. Флагом считается цветное стекло, в моде поддерживаются все цвета, которые есть в игре, поэтому количество команд может быть равно количеству цветов. После установки стекла на истоке луча у всех на экране высвечивается сообщение, информирующее, что флаг установила комнада цвета стекла. Появляется таймер и отсчитывается время, которое было установлено по дефолту(5 минут 30 секунд) или с помощью команды /flagpoint time <минуты> <секунды>. 

Также мод добавляет команду /flagpoint prepare <минуты> <секунды>, которая активирует таймер с указанным временем и непозволяет устанавливать флаг, пока таймер подготовки активен.

В моде я предусмотрел различные попытки закрыть точку для флага, поэтому я предотвращаю какие-либо попытки установить блок или залить чем-либо исток луча, куда устанавливается флаг, а также над ним в пределах 20 блоков. 
В будущем я думаю сделать более гибкую настройку для людей, которые хотят реализоовать свои простые режимы. Также думаю сделать кроме одиночной точки для захвата, которая подходит для режима царь горы, возможность устанавливать несколько точек, что будет отдельной командой /mflagpoint <flag_name> x y z. Чтобы мой мод был более разнообразным и подходил большему числу людей, которые хотят просто прикалываться с друзьями на локальном сервере или для тех, кто создаёт небольшие режимы.

EN
This is an open-source mod for Minecraft 1.7.10. The project itself was created using the @GTNewHorizons starter. I recommend the starter's gradle file; I got it up and running very quickly.

The mod itself is available on Modrinth https://modrinth.com/mod/commandmod. The mod file is also available on my dedicated Discord server https://discord.gg/7Q7hVZGm. You can also play the Glassbreakers mode, which is enabled by this mod, there.

The mod adds the ability to create a flag placement point at specific coordinates (using the /flagpoint x y z command). A white beam will shoot into the sky from a block at these coordinates, marking the flag's location. The flag is considered colored glass; the mod supports all colors in the game, so the number of commands can equal the number of colors. After placing the glass at the source of the beam, a message appears on everyone's screen informing them that the flag was placed by a team matching the color of the glass. A timer appears and counts down the time set by default (5 minutes 30 seconds) or by using the /flagpoint time <minutes> <seconds> command.

The mod also adds the /flagpoint prepare <minutes> <seconds> command, which activates a timer with the specified time and prevents flag placement while the prepare timer is active.

In the mod, I've provided for various attempts to block the flag point, so I prevent any attempts to place a block or flood the beam source where the flag is placed, as well as within 20 blocks above it. In the future, I'm considering adding more flexible settings for people who want to implement their own simple modes. I'm also considering adding the ability to set multiple capture points, in addition to a single capture point, which is suitable for King of the Hill mode, using a separate command: /mflagpoint <flag_name> x y z. To make my mod more diverse and suitable for more people who just want to have fun with friends on a local server or for those who create small modes.
