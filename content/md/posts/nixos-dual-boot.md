{:title "Distro hopping to NixOS in a dual boot system"
 :description "Ein Satz mit x: Es wurde Nix."
 :date "2023-10-21"
 :layout :post
 :tags ["nix" "nixos" "linux"]
 :comments {:instance "chaos.social" :author "johnny" :id "111273212205917832"}}

NixOS has been on my radar for a while. I think I first heard of it around 4 years ago as this really unconventional and unorthodox Linux distribution. I was familiar with the general idea of having an immutable system, pinning versions and all that but didn't pay it much further attention. At that time, I was still very inexperienced with Linux (I still kind of am). Somewhat recently, a lot of people in my online social circles have started using and/or promoting NixOS (or Nix in general), so I started learning more about it and getting an idea of how you actually work with it. I was surprised to see how you could model pretty much any setup in Nix and how people were using it for [literally anything](https://media.ccc.de/v/nixcon-2023-35290-nix-and-kubernetes-deployments-done-right).

If you're just interested in the technicalities of how I did it (e.g. if you want to follow the same steps), skip to the [installation process](#installation-process).

## A needed switch

Coincidentally, I wanted to switch distros on my desktop computer because I was getting tired of Manjaro around the same time. It worked well for me for the most part but occasionally random things would break and I'd have to hunt down weird idiosyncracies to fix them. This was usually not a terrible amount of effort but I just hated running `pacman -Syu` and not knowing if I could continue using my programs as usual or if I'd have to fix something first. When I came home from my semester abroad this year, I was almost scared to run a full upgrade given that I was half a year behind. And to nobody's surprise, it didn't work. It was then that I decided that I was going to ditch Manjaro for good. For various other reasons, see [this text](https://github.com/arindas/manjarno). 

I was and still am pretty happy with my Arch Linux installation on my laptop, and while that has generally been more stable than Manjaro, I wanted something that I could completely rely on on my desktop. I had experience with Debian and Ubuntu derivatives but wasn't too keen on installing those. Some just moved too slow for me, a developer who usually wants the latest software quick, others were too focused on hand-holding for me. Plus I wanted to avoid corporate distros like Ubuntu or Pop_OS! (a monumentally stupid name, by the way) and ideally learn something new on the way. So I decided to install Fedora because that seemed like a nice middle ground between stable, up-to-date, well-supported etc. I talked to people about NixOS as well but stayed clear at that point because I didn't fancy spending a weekend writing configuration.

Well, with Fedora I was already off to a great start: I borked my entire dual boot system somehow. Of course the root cause was user error, but the fact that the Fedora installer is awful compared to what virtually every other distro offers certainly didn't help. So I found myself wiping everything (I had backed up my user home before), repartitioning, installing Fedora first (telling the installer where to install to if there already is something else on the disk makes it angry, apparently) and then installing Windows (which, suprisingly, didn't destroy the existing Linux boot loader like I was warned about). At this point, everything worked again but then the real annoyances started happening *in* Fedora. I experienced everything from constant graphical glitches (sometimes seriously disturbing the use of the desktop), faulty default configuration (e.g. time sync wasn't working, prompting a frustrating evening of debugging why the hell I was getting rate limited in a Discord bot I was writing), broken system components like bluetooth and networking, to complete, unescapable system freezes when trying to suspend. Needless to say, I was not happy and I was not willing to invest a lot of time into this very freshly setup system.

So, I gave in to the tempting calls of the sirens from the Nix cult and started looking into how I could set it up on my machine.

## The NixOS documentation

When you first read about Nix(OS), it really seems like an almost flawless system: it's incredibly versatile and flexible, possibly the most reliable modern operating system ever, backed by a very strong community. But then you get to the documentation and understand why not everybody is using it.

Look, for what it's worth, I don't think the documentation is *horrible* per se. It's more of a combination of things: the fact that the manual is incomplete, that you cannot navigate it very conveniently in the browser, that a lot of options are insufficiently described, that many packages don't have any documentation aside from a short description of the thing they're installing. This is what discouraged me from getting started. Unlike with other distros, there are also no "conventional" guides for how to do things and a higher level of Linux knowledge is usually expected. All of this, combined with the fact that the distro is just *fundamentally different* from essentially everything else in how it works makes the "getting started" experience harder than it needs to be, if you ask me.

As mentioned earlier, I wanted to switch from Fedora to NixOS while keeping my dual boot setup and my user files. The manual describes a way that is supposedly super easy and perfect for a "replace the current distro" operation: *lustrate*. Here's how that's *supposed* to work:

1. Install nix
2. Generate config
3. Create a NIXOS_LUSTRATE file
4. Reboot
5. NixOS moves your old stuff out of the way and installs itself properly on the first boot

As it turns out though, an opaque process that moves the entire root somewhere else and tries to retrofit a boot loader into the previous setup is pretty fragile. I tried setting it up with systemd-boot first, that didn't want to install, then I tried using grub like before which gave me some weird outputs and ended up not finding the kernel after the reboot. When I asked for help, I was advised to just go through a manual installation instead, which was a lot more straight forward (and easier, surprisingly) than whatever I tried to do with lustrate. So, my recommendation is to just do it manually if you're in the same situation as me. At least it's clear what actually happens to your system this way.

Unfortunately, the dual booting info for NixOS that's out there and especially for my setup – an existing Linux in an existing dual boot – is... old, bad, or both. I figured I'd write down the steps that I used and maybe explain some things that I had to learn myself along the way. As always: be careful when you mess with your operating system. Create backups and save them on other disks before you do anything that has the potential to destroy your computer (i.e., the following)!

## Installation process

In the following I'll describe how I got (and how you can get) from another Linux distro to NixOS while preserving your home directory and not touching Windows if you're dual booting.

So, here's my setup before the installation:

- 3 (relevant) partitions:
  - ESP (efi system partition), mounted at `/boot/efi` (whatever you do, keep this safe)
  - Main partition (btrfs), with 2 subvolumes:
    - `root`, mounted at `/`
    - `home`, mounted at `/home`
    - this is essentially equivalent to having two separate ext4 partitions for `/` and `/home`. The general steps should work regardless of file systems, 
    with btrfs you just have to use some slightly different commands.
  - Boot partition (ext4), mounted at `/boot` (this is a weird thing that Fedora set me up with, *your* `/boot` may just be part of the root partition)
- Booted in UEFI mode (not legacy) – this *has* to be the same for you
- Using grub as the boot loader (this shouldn't matter, you'll install a new boot loader anyway)

### 1. Get into a NixOS live image

You've done this before, right? Download a NixOS image and flash it to some USB drive (or a CD, if you're feeling retro). The instructions for how to create one are [in the manual](https://nixos.org/manual/nixos/stable/#sec-booting-from-usb).

The [minimal (non-graphical) image](https://nixos.org/download#nixos-iso) is perfectly fine, since all the following steps are just console commands.

Then, boot into this live image by mashing whatever key your mainboard wants you to hit and changing the boot order in the UEFI/BIOS. Make sure to select the UEFI version if multiple options with your drive's name show up.

### 2. Mount and clear out your previous root partition

In order to install NixOS in the place where your old system used to reside, you should make some space. And by making some space, I mean deleting all of it. The following works if your previous setup had a separation of home and root. If you don't have this and you still want to keep `/home`, you cannot reformat the partition, you either have to delete everything (except `/home`) manually or save `/home` somewhere and repartition your one `/` partition into two separate ones (which is what I would recommend). 

#### With root and home btrfs subvolumes

If your system is set up using btrfs and your home and root "partitions" are actually separate subvolumes on the same partition, here's how you can clear the root partition:

1. Mount the entire btrfs partition: `sudo mount /dev/xxx /mnt` (you should be able to figure out the device name from the `lsblk` output)
2. Check the subvolumes to confirm your suspicions: `sudo btrfs subvol list /mnt` (this should give you root and home)
3. Delete the root subvolume: `sudo btrfs subvol delete /mnt/root` (if this fails because the "directory is not empty", check if there are subvolumes *inside* the root subvolume and delete those first: `sudo btrfs subvol list /mnt/root`)
4. Recreate the root subvolume: `sudo btrfs subvol create /mnt/root`
5. Unmount the btrfs partition: `sudo umount /mnt`
6. Mount the subvolumes separately now, so NixOS gets the setup it is expecting
   - `sudo mount /dev/xxx /mnt -o subvolume=root` (root -> `/mnt`)
   - `sudo mount /dev/xxx /mnt/home -o subvolume=home` (home -> `/mnt/home`)
   - `sudo mount /dev/yyy /mnt/boot` (the device here is the EFI system partition, which NixOS wants to have in `/boot`)

You may need to `mkdir` the mountpoints first. 

#### With separate root and home partitions

This works similar to the btrfs way, except you handle your separate partitions individually. The steps for this are basically described in the [manual](https://nixos.org/manual/nixos/stable/#sec-installation-manual), but here's a rundown, ignoring the steps that are necessary on a completely empty system:

1. Reformat your previous root partition: `mkfs.ext4 -L nixos /dev/xxx` (use the device name of your root partition, check `lsblk`)
2. Mount the partitions like so:
   - `sudo mount /dev/xxx /mnt` (root -> `/mnt`)
   - `sudo mount /dev/yyy /mnt/home` (home -> `/mnt/home`, the device is that of your home partition)
   - `sudo mount /dev/zzz /mnt/boot` (the device here is the EFI system partition, which NixOS wants to have in `/boot`)

You may need to `mkdir` the mountpoints first.

### 3. Configure the system

Now, `/mnt` should be empty except for a `boot` directory with the ESP mounted and a `home` directory with the home subvolume/partition mounted. If you, like me, still have a boot partition lying around, just ignore it, it's not necessary anymore. You don't have to mount it and you can delete it later. The reason is that grub needs extra files in `/boot`, while systemd-boot (the boot loader we are going to install) installs directly into the ESP, no further files needed. This is why on NixOS with systemd-boot as the default, the ESP is mounted at `/boot`, whereas in distros that use grub, the ESP is mounted at `/boot/efi` by default.

Now would also be a good time to mount other fixed disks that you have installed in your system. For example, on my system I have an additional HDD that I want to have at `/mnt/hdd` in my file system. So, in the live image, I would mount it at `/mnt/mnt/hdd` (`/mnt` is the "new system root"). The reason why we're already mounting everything even though we don't need it for the installation (like `/home` as well) is that this will then get picked up by the configuration generator and configured automatically.

After this point, the steps follow the manual: run `sudo nixos-generate-config --root /mnt` and then edit `/mnt/etc/nixos/configuration.nix` to configure your system. The comments in that file give some helpful tips and the manual has a [section about configuration](https://nixos.org/manual/nixos/stable/#ch-configuration) that you should at least skim, even though it is by no means exhaustive or sufficient to create a whole config. 

You can look at [my initial config](https://codeberg.org/johnnyjayjay/nixos-config/src/commit/efe3b3adf7be69a29329b8ca78b6a803b9e6ba47/configuration.nix) for inspiration. It's not very long and covers everything I need in my system. The main things that you're most likely also interested in and that are not mentioned in the default config:

- pipewire instead of pulseaudio:
  ```nix 
  # Enable sound via pipewire
  security.rtkit.enable = true;
  services.pipewire = {
    enable = true;
    alsa.enable = true;
    alsa.support32Bit = true;
    pulse.enable = true;
    jack.enable = true;
  };
  ```
- KDE desktop environment:
  ```nix
  services.xserver = {
    enable = true;
    layout = "eu"; # keyboard layout
    desktopManager.plasma5.enable = true;
    displayManager.sddm.enable = true;
  };
  ```
- Allowing unfree (proprietary) packages, in my case Discord and Spotify:
  ```nix
  nixpkgs.config.allowUnfreePredicate = pkg:
    builtins.elem (lib.getName pkg) [ "discord" "spotify" ];
  ```

Go through the generated config, read the comments and look up possible options in the [NixOS options search](https://search.nixos.org/options). Make sure to enable the `systemd-boot` boot loader, as that is what is strictly required for the installation to work with the preparations we did.

### 4. Install

Run `sudo nixos-install`. That's it. After rebooting, you should see systemd-boot and be able to select from the list of operating systems. In my case, Windows and NixOS. Welcome to the cult, I guess?
