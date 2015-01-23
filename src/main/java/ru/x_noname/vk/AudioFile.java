/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.x_noname.vk;

/**
 *
 * @author Ivan (X-NoNAME) Kazakov
 * @mailto mail@x-noname.ru
 */
class AudioFile {
    final String name;
    final String link;
    private boolean status;

    public AudioFile(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public void setStatus(boolean b) {
        status=true;
    }

    public boolean isStatus() {
        return status;
    }

    

}
