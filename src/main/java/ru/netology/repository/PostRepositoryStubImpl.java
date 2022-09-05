package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PostRepositoryStubImpl implements PostRepository {
    private final AtomicLong newId = new AtomicLong();                 //хранит id по порядку
    private final Map<Long, Post> posts = new ConcurrentHashMap<>();

    public List<Post> all() {
        return new ArrayList<>(posts.values());
    }

    public Optional<Post> getById(long id) {
        return Optional.of(posts.get(id));
    }

    public Post save(Post post) {
        if (posts.isEmpty()) {              //если список постов пустой, то
            if (post.getId() == 0) {            //если пришел пост с id = 0
                post.setId(newId.incrementAndGet());   //присваиваем посту id = 1 (самый первый случай)
                posts.put(post.getId(), post);          //и добавляем его в лист
            } else {                            //иначе, если id поста != 0, то
                posts.put(post.getId(), post);      //просто добавляем пост с этим id в список постов
            }
        } else {                            //иначе если список постов не пустой, то
            if (post.getId() == 0) {            //если пришел пост с id = 0
                newId.set(1);                              //устанавливаем id = 1
                while (posts.containsKey(newId.get())) {   //и пробегаемся по ключам мапы до тех пор,
                    newId.incrementAndGet();               //пока не будет подобран следующий по порядку id
                }
                post.setId(newId.get());                   //присваиваем посту новый id
                posts.put(post.getId(), post);          //кладем пост в мапу
            } else {                            //иначе, если пришел пост с id != 0, то
                if (posts.containsKey(post.getId())) {  //смотрим, есть ли уже такой id в мапе, если да, то
                    if (!posts.get(post.getId()).equals(post)) {    //если старый пост и новый пост отличаются
                        posts.replace(post.getId(), post);              //заменяем старый пост новым
                    }                                               //иначе ничего не делаем
                } else {                                //иначе, если поста с таки id не существует, то
                    posts.put(post.getId(), post);                  //добавляем пост в мапу
                }
            }
        }
        return post;
    }

    public void removeById(long id) {
        if (posts.containsKey(id)) {        //если id содержится в списке ключей мапы
            posts.remove(id);                   //удаляем пост из мапы
        } else {                            //иначе если поста с таким id нет в мапе
            throw new NotFoundException();      //выбрасываем ошибку в контроллер
        }
        if (posts.isEmpty()) {              //если удалили все посты из списка
            newId.set(0);                       //выставляем нумерацию на 0
        }
    }
}